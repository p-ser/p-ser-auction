package com.pser.auction.application;

import static io.vavr.API.$;
import static io.vavr.API.Case;
import static io.vavr.API.Match;
import static io.vavr.API.run;
import static io.vavr.Predicates.is;

import com.pser.auction.dao.AuctionDao;
import com.pser.auction.domain.Auction;
import com.pser.auction.domain.AuctionStatusEnum;
import com.pser.auction.dto.AuctionCreateRequest;
import com.pser.auction.dto.AuctionDto;
import com.pser.auction.dto.AuctionMapper;
import com.pser.auction.exception.NotOngoingAuctionException;
import com.pser.auction.infra.kafka.producer.AuctionStatusProducer;
import io.vavr.control.Try;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuctionService {
    private final AuctionDao auctionDao;
    private final AuctionMapper auctionMapper;
    private final AuctionStatusProducer auctionStatusProducer;

    public long save(AuctionCreateRequest request) {
        Auction auction = auctionMapper.toEntity(request);
        return Try.of(() -> auctionDao.save(auction))
                .onSuccess((savedAuction) -> auctionStatusProducer.produceCreated(auctionMapper.toDto(savedAuction)))
                .recover((e) -> auctionDao.findAuctionByReservationId(request.getReservationId())
                        .orElseThrow())
                .get()
                .getId();
    }

    @Transactional
    public void updateToOngoing(long auctionId) {
        Auction auction = auctionDao.findById(auctionId)
                .orElseThrow();
        AuctionStatusEnum targetStatus = AuctionStatusEnum.ONGOING;
        auction.updateStatus(targetStatus);
    }

    @Transactional
    public Long closeAuction(long auctionId) {
        Auction auction = auctionDao.findById(auctionId)
                .orElseThrow();
        AuctionDto auctionDto = auctionMapper.toDto(auction);
        AuctionStatusEnum status = auction.getStatus();

        Runnable whenNoBid = () -> {
            auction.updateStatus(AuctionStatusEnum.FAILURE);
            auctionStatusProducer.produceFailure(auctionDto);
        };
        Runnable whenAnyBid = () -> {
            auction.updateStatus(AuctionStatusEnum.PAYMENT_AWAITING);
            auction.updateWinner();
            auctionStatusProducer.producePaymentAwaiting(auctionDto);
        };
        Runnable whenCreatedStatus = () -> {
            auctionDao.delete(auction);
            auctionStatusProducer.produceCreatedRollback(auctionDto);
            throw new NotOngoingAuctionException();
        };
        Runnable whenOngoingStatus = () -> {
            boolean isEmpty = auction.getBids().isEmpty();
            Match(isEmpty).of(
                    Case($(true), () -> run(whenNoBid)),
                    Case($(), () -> run(whenAnyBid))
            );
        };
        Runnable whenElseStatus = () -> {
            throw new NotOngoingAuctionException();
        };

        Match(status).of(
                Case($(is(AuctionStatusEnum.CREATED)), () -> run(whenCreatedStatus)),
                Case($(is(AuctionStatusEnum.ONGOING)), () -> run(whenOngoingStatus)),
                Case($(), () -> run(whenElseStatus))
        );

        return auction.getWinnerId();
    }

    public void delete(long auctionId) {
        Auction auction = auctionDao.findById(auctionId)
                .orElseThrow();
        auctionDao.delete(auction);
    }
}
