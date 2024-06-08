package com.pser.auction.application;

import com.pser.auction.dao.AuctionDao;
import com.pser.auction.dao.DepositDao;
import com.pser.auction.domain.Auction;
import com.pser.auction.domain.AuctionStatusEnum;
import com.pser.auction.domain.Deposit;
import com.pser.auction.domain.DepositStatusEnum;
import com.pser.auction.dto.DepositCreateRequest;
import com.pser.auction.dto.DepositMapper;
import com.pser.auction.dto.DepositResponse;
import com.pser.auction.dto.PaymentDto;
import com.pser.auction.dto.RefundDto;
import com.pser.auction.dto.StatusUpdateDto;
import com.pser.auction.exception.SameStatusException;
import com.pser.auction.exception.ValidationFailedException;
import com.pser.auction.infra.kafka.producer.DepositStatusProducer;
import io.vavr.control.Try;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DepositService {
    private final DepositMapper depositMapper;
    private final AuctionDao auctionDao;
    private final DepositDao depositDao;
    private final DepositStatusProducer depositStatusProducer;

    public DepositResponse getByMerchantUid(String merchantUid) {
        Deposit deposit = depositDao.findByMerchantUid(merchantUid)
                .orElseThrow();
        return depositMapper.toResponse(deposit);
    }

    public Page<DepositResponse> getAllByAuctionId(long auctionId, Pageable pageable) {
        return depositDao.findAllByAuctionId(auctionId, pageable)
                .map(depositMapper::toResponse);
    }

    @Transactional
    public DepositResponse getOrSave(DepositCreateRequest request) {
        Auction auction = findOngoingAuctionById(request.getAuctionId());

        return findPendingDepositByUserIdAndAuctionId(request)
                .map(depositMapper::toResponse)
                .orElseGet(() -> {
                    request.setAuction(auction);
                    Deposit deposit = depositMapper.toEntity(request);
                    deposit.addOnCreatedEventHandler(
                            d -> depositStatusProducer.produceCreated(((Deposit) d).getMerchantUid()));
                    deposit = depositDao.save(deposit);
                    return depositMapper.toResponse(deposit);
                });
    }

    @Transactional
    public DepositStatusEnum checkPayment(long depositId, String impUid) {
        Deposit deposit = depositDao.findById(depositId)
                .orElseThrow();
        DepositStatusEnum status = deposit.getStatus();

        if (status.equals(DepositStatusEnum.CREATED)) {
            PaymentDto paymentDto = PaymentDto.builder()
                    .impUid(impUid)
                    .amount(deposit.getPrice())
                    .merchantUid(deposit.getMerchantUid())
                    .build();
            deposit.addOnUpdatedEventHandler(d -> depositStatusProducer.producePaymentValidationRequired(paymentDto));
            updateToPaymentValidationRequired(paymentDto);
        }
        return status;
    }

    @Transactional
    public void updateStatus(StatusUpdateDto<DepositStatusEnum> statusUpdateDto) {
        updateStatus(statusUpdateDto, null);
    }

    @Transactional
    public void updateStatus(StatusUpdateDto<DepositStatusEnum> statusUpdateDto, Consumer<Deposit> validator) {
        Deposit reservation = depositDao.findById(statusUpdateDto.getId())
                .orElseThrow();
        DepositStatusEnum targetStatus = statusUpdateDto.getTargetStatus();

        if (validator != null) {
            validator.accept(reservation);
        }

        reservation.updateStatus(targetStatus);
    }

    @Transactional
    public void rollbackStatus(StatusUpdateDto<DepositStatusEnum> statusUpdateDto) {
        rollbackStatus(statusUpdateDto, null);
    }

    @Transactional
    public void rollbackStatus(StatusUpdateDto<DepositStatusEnum> statusUpdateDto, Consumer<Deposit> validator) {
        Deposit reservation = depositDao.findById(statusUpdateDto.getId())
                .orElseThrow();
        DepositStatusEnum targetStatus = statusUpdateDto.getTargetStatus();

        if (validator != null) {
            validator.accept(reservation);
        }

        reservation.rollbackStatusTo(targetStatus);
    }

    @Transactional
    public void updateToPaymentValidationRequired(PaymentDto paymentDto) {
        Try.run(() -> {
                    StatusUpdateDto<DepositStatusEnum> statusUpdateDto = StatusUpdateDto.<DepositStatusEnum>builder()
                            .merchantUid(paymentDto.getMerchantUid())
                            .targetStatus(DepositStatusEnum.PAYMENT_VALIDATION_REQUIRED)
                            .build();
                    updateStatus(statusUpdateDto, deposit -> {
                        int paidAmount = paymentDto.getAmount();

                        if (deposit.getPrice() != paidAmount) {
                            throw new ValidationFailedException("결제 금액 불일치");
                        }
                        deposit.updateImpUid(paymentDto.getImpUid());
                    });
                })
                .recover(SameStatusException.class, e -> null)
                .get();
    }

    @Transactional
    public void updateToRefundRequired(String merchantUid) {
        StatusUpdateDto<DepositStatusEnum> statusUpdateDto = StatusUpdateDto.<DepositStatusEnum>builder()
                .merchantUid(merchantUid)
                .targetStatus(DepositStatusEnum.REFUND_REQUIRED)
                .build();
        updateStatus(statusUpdateDto);

        RefundDto refundDto = RefundDto.builder()
                .merchantUid(merchantUid)
                .build();
        depositStatusProducer.produceRefundRequired(refundDto);
    }

    @Transactional
    public void refundAllExceptWinner(long auctionId, Long winnerId) {
        List<DepositStatusEnum> statusEnums = List.of(
                DepositStatusEnum.CREATED,
                DepositStatusEnum.PAYMENT_VALIDATION_REQUIRED,
                DepositStatusEnum.PAID
        );
        depositDao.findAllByAuctionIdAndStatusIn(auctionId, statusEnums)
                .forEach(deposit -> {
                    if (winnerId == null || winnerId.equals(deposit.getUserId())) {
                        return;
                    }
                    updateToRefundRequired(deposit.getMerchantUid());
                });
    }

    private Auction findOngoingAuctionById(Long auctionId) {
        Auction auction = auctionDao.findById(auctionId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 경매입니다"));
        AuctionStatusEnum status = auction.getStatus();
        if (!status.equals(AuctionStatusEnum.CREATED)) {
            throw new IllegalArgumentException("진행중인 경매가 아닙니다");
        }
        return auction;
    }

    private Optional<Deposit> findPendingDepositByUserIdAndAuctionId(DepositCreateRequest request) {
        Long userId = request.getUserId();
        Long auctionId = request.getAuctionId();
        List<DepositStatusEnum> pendingStatuses = List.of(
                DepositStatusEnum.PAYMENT_VALIDATION_REQUIRED,
                DepositStatusEnum.CREATED
        );
        return depositDao.findAllByUserIdAndAuctionIdAndStatusIn(userId, auctionId, pendingStatuses)
                .stream()
                .findFirst();
    }
}
