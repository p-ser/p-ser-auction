package com.pser.auction.application;

import com.pser.auction.dao.AuctionDao;
import com.pser.auction.domain.Auction;
import com.pser.auction.dto.AuctionCreateRequest;
import com.pser.auction.dto.AuctionMapper;
import com.pser.auction.infra.kafka.producer.AuctionCreatedProducer;
import io.vavr.control.Try;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuctionService {
    private final AuctionDao auctionDao;
    private final AuctionMapper auctionMapper;
    private final AuctionCreatedProducer auctionCreatedProducer;

    public long save(AuctionCreateRequest request) {
        Auction auction = auctionMapper.toEntity(request);
        return Try.of(() -> auctionDao.save(auction))
                .onSuccess((savedAuction) -> auctionCreatedProducer.notifyCreated(auctionMapper.toDto(savedAuction)))
                .recover((e) -> auctionDao.findAuctionByReservationId(request.getReservationId())
                        .orElseThrow())
                .get()
                .getId();
    }

    public void closeAuction(long auctionId) {

    }
}
