package com.pser.auction.application;

import static io.vavr.API.$;
import static io.vavr.API.Case;
import static io.vavr.API.Match;
import static io.vavr.API.run;
import static io.vavr.Predicates.is;

import com.pser.auction.dao.AuctionDao;
import com.pser.auction.domain.Auction;
import com.pser.auction.domain.AuctionStatusEnum;
import com.pser.auction.domain.ReservationStatusEnum;
import com.pser.auction.dto.AuctionCreateRequest;
import com.pser.auction.dto.AuctionDto;
import com.pser.auction.dto.AuctionMapper;
import com.pser.auction.dto.PaymentDto;
import com.pser.auction.dto.RefundDto;
import com.pser.auction.dto.ReservationResponse;
import com.pser.auction.exception.NotOngoingAuctionException;
import com.pser.auction.exception.ValidationFailedException;
import com.pser.auction.infra.HotelClient;
import com.pser.auction.infra.kafka.producer.AuctionStatusProducer;
import io.vavr.control.Try;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
    private final HotelClient hotelClient;

    @Transactional
    public long save(AuctionCreateRequest request) {
        ReservationResponse reservationResponse = hotelClient.getReservationById(request.getReservationId());
        validateAuctionCreateRequest(request, reservationResponse);

        int depositPrice = (int) (reservationResponse.getPrice() / 0.05);
        LocalDateTime auctionEndAt = LocalDateTime.of(reservationResponse.getStartAt(), LocalTime.MIN);
        request.setDepositPrice(depositPrice);
        request.setEndAt(auctionEndAt);
        return Try.of(() -> {
                    Auction auction = auctionMapper.toEntity(request);
                    return auctionDao.save(auction);
                })
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
    public void rollbackToPaymentRequired(String merchantUid) {
        Auction auction = auctionDao.findByMerchantUid(merchantUid)
                .orElseThrow();
        AuctionStatusEnum status = auction.getStatus();
        AuctionStatusEnum targetStatus = AuctionStatusEnum.PAYMENT_REQUIRED;

        if (!status.equals(targetStatus)) {
            auction.updateStatus(targetStatus);
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
                Case($(), () -> run(whenElseStatus))
        );

        return auction.getWinnerId();
    }

    public void delete(long auctionId) {
        Auction auction = auctionDao.findById(auctionId)
                .orElseThrow();
        auctionDao.delete(auction);
    }

    private void validateAuctionCreateRequest(AuctionCreateRequest request, ReservationResponse reservationResponse) {
        ReservationStatusEnum reservationStatus = reservationResponse.getStatus();
        LocalDateTime reservationDDayDateTime = LocalDateTime.of(reservationResponse.getStartAt(), LocalTime.MIN);
        LocalDateTime oneHourFromNow = LocalDateTime.now().plusHours(1);

        if (!reservationStatus.equals(ReservationStatusEnum.BEFORE_CHECKIN)) {
            throw new ValidationFailedException("경매할 수 없는 상태의 예약입니다");
        }
        if (oneHourFromNow.isAfter(reservationDDayDateTime)) {
            throw new ValidationFailedException("체크인 일자 자정으로부터 1시간 전까지만 경매할 수 있습니다");
        }
    }
}
