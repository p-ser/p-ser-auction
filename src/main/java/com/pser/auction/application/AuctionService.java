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
import com.pser.auction.dto.PaymentDto;
import com.pser.auction.dto.RefundDto;
import com.pser.auction.exception.NotOngoingAuctionException;
import com.pser.auction.exception.ValidationFailedException;
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
    public AuctionStatusEnum checkPayment(long auctionId, String impUid) {
        Auction auction = auctionDao.findById(auctionId)
                .orElseThrow();
        AuctionStatusEnum status = auction.getStatus();

        if (status.equals(AuctionStatusEnum.PAYMENT_REQUIRED)) {
            PaymentDto paymentDto = PaymentDto.builder()
                    .impUid(impUid)
                    .amount(auction.getEndPrice())
                    .merchantUid(auction.getMerchantUid())
                    .build();
            updateToPaymentValidationRequired(paymentDto);
        }
        return status;
    }

    @Transactional
    public void updateToOngoing(long auctionId) {
        Auction auction = auctionDao.findById(auctionId)
                .orElseThrow();
        AuctionStatusEnum targetStatus = AuctionStatusEnum.ONGOING;
        auction.updateStatus(targetStatus);
    }

    @Transactional
    public void updateToPaid(PaymentDto paymentDto) {
        Auction auction = auctionDao.findByMerchantUid(paymentDto.getMerchantUid())
                .orElseThrow();
        AuctionStatusEnum status = auction.getStatus();
        AuctionStatusEnum targetStatus = AuctionStatusEnum.PAID;
        int paidAmount = paymentDto.getAmount();

        if (auction.getEndPrice() != paidAmount) {
            throw new ValidationFailedException("결제 금액 불일치");
        }

        if (status.equals(AuctionStatusEnum.PAYMENT_VALIDATION_REQUIRED)) {
            auction.updateStatus(targetStatus);
        }
    }

    @Transactional
    public void updateToPaymentRequired(PaymentDto paymentDto) {
        Auction auction = auctionDao.findByMerchantUid(paymentDto.getMerchantUid())
                .orElseThrow();
        AuctionStatusEnum status = auction.getStatus();
        AuctionStatusEnum targetStatus = AuctionStatusEnum.PAYMENT_REQUIRED;

        if (!status.equals(targetStatus)) {
            auction.updateStatus(targetStatus);
        }
    }

    @Transactional
    public void updateToPaymentValidationRequired(PaymentDto paymentDto) {
        Auction auction = auctionDao.findByMerchantUid(paymentDto.getMerchantUid())
                .orElseThrow();
        AuctionStatusEnum status = auction.getStatus();
        AuctionStatusEnum targetStatus = AuctionStatusEnum.PAYMENT_VALIDATION_REQUIRED;

        int paidAmount = paymentDto.getAmount();

        if (auction.getEndPrice() != paidAmount) {
            throw new ValidationFailedException("결제 금액 불일치");
        }

        if (status.equals(AuctionStatusEnum.PAYMENT_REQUIRED)) {
            auction.updateImpUid(paymentDto.getImpUid());
            auction.updateStatus(targetStatus);
            auctionStatusProducer.producePaymentValidationRequired(paymentDto);
        }
    }

    @Transactional
    public void updateToRefundRequired(PaymentDto paymentDto) {
        Auction auction = auctionDao.findByMerchantUid(paymentDto.getMerchantUid())
                .orElseThrow();
        AuctionStatusEnum targetStatus = AuctionStatusEnum.REFUND_REQUIRED;

        if (!targetStatus.equals(auction.getStatus())) {
            auction.updateStatus(targetStatus);
            RefundDto refundDto = RefundDto.builder()
                    .impUid(paymentDto.getImpUid())
                    .merchantUid(paymentDto.getMerchantUid())
                    .build();
            auctionStatusProducer.produceRefundRequired(refundDto);
        }
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
            auction.updateStatus(AuctionStatusEnum.PAYMENT_REQUIRED);
            auction.updateWinner();
            auctionStatusProducer.producePaymentRequired(auctionDto);
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
