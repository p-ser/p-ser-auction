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
import com.pser.auction.exception.ValidationFailedException;
import com.pser.auction.infra.kafka.producer.DepositStatusProducer;
import java.util.List;
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
        Auction auction = findOnGoingAuctionById(request.getAuctionId());

        Deposit deposit = findPendingDepositByUserIdAndAuctionId(request.getUserId(), request.getAuctionId());
        if (deposit != null) {
            return depositMapper.toResponse(deposit);
        }

        request.setAuction(auction);
        deposit = depositMapper.toEntity(request);
        deposit = depositDao.save(deposit);
        depositStatusProducer.produceCreated(deposit.getMerchantUid());
        return depositMapper.toResponse(deposit);
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
            updateToPaymentValidationRequired(paymentDto);
        }
        return status;
    }

    @Transactional
    public void rollbackToCreated(String merchantUid) {
        Deposit deposit = depositDao.findByMerchantUid(merchantUid)
                .orElseThrow();
        deposit.updateStatus(DepositStatusEnum.CREATED);
    }

    @Transactional
    public void updateToPaymentValidationRequired(PaymentDto paymentDto) {
        Deposit deposit = depositDao.findByMerchantUid(paymentDto.getMerchantUid())
                .orElseThrow();
        DepositStatusEnum targetStatus = DepositStatusEnum.PAYMENT_VALIDATION_REQUIRED;
        int paidAmount = paymentDto.getAmount();

        if (deposit.getPrice() != paidAmount) {
            throw new ValidationFailedException("결제 금액 불일치");
        }

        if (!targetStatus.equals(deposit.getStatus())) {
            deposit.updateImpUid(paymentDto.getImpUid());
            deposit.updateStatus(targetStatus);
            depositStatusProducer.producePaymentValidationRequired(paymentDto);
        }
    }

    @Transactional
    public void updateToRefundRequired(RefundDto refundDto) {
        Deposit deposit = depositDao.findByMerchantUid(refundDto.getMerchantUid())
                .orElseThrow();
        DepositStatusEnum targetStatus = DepositStatusEnum.REFUND_REQUIRED;

        if (!targetStatus.equals(deposit.getStatus())) {
            deposit.updateStatus(targetStatus);
            depositStatusProducer.produceRefundRequired(refundDto);
        }
    }

    @Transactional
    public void updateToRefundRequired(String merchantUid) {
        updateToRefundRequired(toRefundDto(merchantUid));
    }

    @Transactional
    public void updateToRefundRequired(PaymentDto paymentDto) {
        updateToRefundRequired(toRefundDto(paymentDto));
    }

    @Transactional
    public void updateToPaid(PaymentDto paymentDto) {
        Deposit deposit = depositDao.findByMerchantUid(paymentDto.getMerchantUid())
                .orElseThrow();
        DepositStatusEnum status = deposit.getStatus();
        DepositStatusEnum targetStatus = DepositStatusEnum.PAID;
        int paidAmount = paymentDto.getAmount();

        if (deposit.getPrice() != paidAmount) {
            throw new ValidationFailedException("결제 금액 불일치");
        }

        if (status.equals(DepositStatusEnum.PAYMENT_VALIDATION_REQUIRED)) {
            deposit.updateStatus(targetStatus);
        }
    }

    @Transactional
    public void updateToRefunded(PaymentDto paymentDto) {
        Deposit deposit = depositDao.findByMerchantUid(paymentDto.getMerchantUid())
                .orElseThrow();
        DepositStatusEnum status = deposit.getStatus();
        DepositStatusEnum targetStatus = DepositStatusEnum.REFUNDED;

        if (!targetStatus.equals(status)) {
            deposit.updateStatus(targetStatus);
        }
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

    private RefundDto toRefundDto(PaymentDto paymentDto) {
        return RefundDto.builder()
                .impUid(paymentDto.getImpUid())
                .merchantUid(paymentDto.getMerchantUid())
                .build();
    }

    private RefundDto toRefundDto(String merchantUid) {
        return RefundDto.builder()
                .merchantUid(merchantUid)
                .build();
    }

    private Auction findOnGoingAuctionById(Long auctionId) {
        Auction auction = auctionDao.findById(auctionId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 경매입니다"));
        if (!AuctionStatusEnum.ONGOING.equals(auction.getStatus())) {
            throw new IllegalArgumentException("진행중인 경매가 아닙니다");
        }
        return auction;
    }

    private Deposit findPendingDepositByUserIdAndAuctionId(Long userId, Long auctionId) {
        List<DepositStatusEnum> pendingStatuses = List.of(
                DepositStatusEnum.PAYMENT_VALIDATION_REQUIRED,
                DepositStatusEnum.CREATED
        );
        return depositDao.findByUserIdAndAuctionIdAndStatusIn(userId, auctionId, pendingStatuses)
                .orElse(null);
    }
}
