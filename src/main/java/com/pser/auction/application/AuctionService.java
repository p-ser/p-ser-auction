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
import com.pser.auction.dto.ReservationResponse;
import com.pser.auction.dto.StatusUpdateDto;
import com.pser.auction.exception.NotOngoingAuctionException;
import com.pser.auction.exception.SameStatusException;
import com.pser.auction.exception.ValidationFailedException;
import com.pser.auction.infra.HotelClient;
import com.pser.auction.infra.kafka.producer.AuctionStatusProducer;
import io.vavr.control.Try;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.function.Consumer;
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

    public AuctionDto getByMerchantUid(String merchantUid) {
        Auction auction = auctionDao.findByMerchantUid(merchantUid)
                .orElseThrow();
        return auctionMapper.toDto(auction);
    }

    public AuctionDto getById(long auctionId) {
        Auction auction = auctionDao.findById(auctionId)
                .orElseThrow();
        return auctionMapper.toDto(auction);
    }

    @Transactional
    public long save(AuctionCreateRequest request) {
        return Try.of(() -> auctionDao.findAuctionByReservationId(request.getReservationId())
                        .orElseThrow())
                .recover((e) -> {
                    ReservationResponse reservationResponse = hotelClient.getReservationById(
                            request.getReservationId());
                    validateAuctionCreateRequest(reservationResponse);

                    int depositPrice = (int) (reservationResponse.getPrice() * 0.05);
                    LocalDateTime auctionEndAt = LocalDateTime.of(reservationResponse.getStartAt(), LocalTime.MIN);
                    request.setPrice((int) (reservationResponse.getPrice() * 0.1));
                    request.setReservationPrice(reservationResponse.getPrice());
                    request.setDepositPrice(depositPrice);
                    request.setEndAt(auctionEndAt);

                    Auction auction = auctionMapper.toEntity(request);
                    auction.addOnCreatedEventHandler(
                            a -> auctionStatusProducer.produceCreated(auctionMapper.toDto((Auction) a)));
                    return auctionDao.save(auction);
                })
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
            auction.addOnUpdatedEventHandler(a -> auctionStatusProducer.producePaymentValidationRequired(paymentDto));
            updateToPaymentValidationRequired(paymentDto);
        }
        return status;
    }

    @Transactional
    public void updateStatus(StatusUpdateDto<AuctionStatusEnum> statusUpdateDto) {
        updateStatus(statusUpdateDto, null);
    }

    @Transactional
    public void updateStatus(StatusUpdateDto<AuctionStatusEnum> statusUpdateDto, Consumer<Auction> validator) {
        Auction auction;
        if (statusUpdateDto.getId() != null) {
            auction = auctionDao.findById(statusUpdateDto.getId())
                    .orElseThrow();
        } else {
            auction = auctionDao.findByMerchantUid(statusUpdateDto.getMerchantUid())
                    .orElseThrow();
        }
        AuctionStatusEnum targetStatus = statusUpdateDto.getTargetStatus();

        if (validator != null) {
            validator.accept(auction);
        }

        auction.addOnUpdatedEventHandler(a -> auctionStatusProducer.produceUpdated(auctionMapper.toDto((Auction) a)));
        auction.updateStatus(targetStatus);
    }

    @Transactional
    public void rollbackStatus(StatusUpdateDto<AuctionStatusEnum> statusUpdateDto) {
        rollbackStatus(statusUpdateDto, null);
    }

    @Transactional
    public void rollbackStatus(StatusUpdateDto<AuctionStatusEnum> statusUpdateDto, Consumer<Auction> validator) {
        Auction auction = auctionDao.findById(statusUpdateDto.getId())
                .orElseThrow();
        AuctionStatusEnum targetStatus = statusUpdateDto.getTargetStatus();

        if (validator != null) {
            validator.accept(auction);
        }

        auction.addOnUpdatedEventHandler(a -> auctionStatusProducer.produceUpdated(auctionMapper.toDto((Auction) a)));
        auction.rollbackStatusTo(targetStatus);
    }

    @Transactional
    public void updateToPaymentValidationRequired(PaymentDto paymentDto) {
        Try.run(() -> {
                    StatusUpdateDto<AuctionStatusEnum> statusUpdateDto = StatusUpdateDto.<AuctionStatusEnum>builder()
                            .merchantUid(paymentDto.getMerchantUid())
                            .targetStatus(AuctionStatusEnum.PAYMENT_VALIDATION_REQUIRED)
                            .build();
                    updateStatus(statusUpdateDto, auction -> {
                        int paidAmount = paymentDto.getAmount();

                        if (auction.getEndPrice() != paidAmount) {
                            throw new ValidationFailedException("결제 금액 불일치");
                        }
                        auction.updateImpUid(paymentDto.getImpUid());
                    });
                })
                .recover(SameStatusException.class, e -> null)
                .get();
    }

    @Transactional
    public Long closeAuction(long auctionId) {
        Auction auction = auctionDao.findById(auctionId)
                .orElseThrow();
        AuctionStatusEnum status = auction.getStatus();

        Runnable whenNoBid = () -> {
            auction.addOnUpdatedEventHandler(a -> auctionStatusProducer.produceNoBid(auctionMapper.toDto((Auction) a)));
            auction.updateStatus(AuctionStatusEnum.NO_BID);
        };
        Runnable whenAnyBid = () -> {
            auction.addOnUpdatedEventHandler(
                    a -> auctionStatusProducer.producePaymentRequired(auctionMapper.toDto((Auction) a)));
            auction.updateStatus(AuctionStatusEnum.PAYMENT_REQUIRED);
            auction.updateWinner();
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

    @Transactional
    public void delete(long auctionId) {
        Auction auction = auctionDao.findById(auctionId)
                .orElseThrow();
        auction.addOnDeletedEventHandler(a -> auctionStatusProducer.produceDeleted(auctionMapper.toDto((Auction) a)));
        auctionDao.delete(auction);
    }

    private void validateAuctionCreateRequest(ReservationResponse reservationResponse) {
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
