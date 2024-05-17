package com.pser.auction.application;

import com.pser.auction.dao.AuctionDao;
import com.pser.auction.dao.DepositDao;
import com.pser.auction.domain.Auction;
import com.pser.auction.domain.AuctionStatusEnum;
import com.pser.auction.domain.Deposit;
import com.pser.auction.domain.DepositStatusEnum;
import com.pser.auction.dto.ConfirmDto;
import com.pser.auction.dto.DepositCreateRequest;
import com.pser.auction.dto.DepositMapper;
import com.pser.auction.dto.DepositResponse;
import com.pser.auction.dto.PaymentDto;
import com.pser.auction.dto.PaymentDto.Response;
import com.pser.auction.dto.RefundDto;
import com.pser.auction.exception.ValidationFailedException;
import com.pser.auction.infra.kafka.producer.DepositConfirmAwaitingProducer;
import com.pser.auction.infra.kafka.producer.DepositCreatedProducer;
import com.pser.auction.infra.kafka.producer.DepositRefundAwaitingProducer;
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
    private final DepositConfirmAwaitingProducer depositConfirmAwaitingProducer;
    private final DepositRefundAwaitingProducer depositRefundAwaitingProducer;
    private final DepositCreatedProducer depositCreatedProducer;

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
        depositCreatedProducer.produce(deposit.getMerchantUid());
        return depositMapper.toResponse(deposit);
    }

    @Transactional
    public DepositStatusEnum checkStatus(long depositId, String impUid) {
        Deposit deposit = depositDao.findById(depositId)
                .orElseThrow();
        DepositStatusEnum status = deposit.getStatus();

        if (status.equals(DepositStatusEnum.CREATED)) {
            ConfirmDto confirmDto = ConfirmDto.builder()
                    .impUid(impUid)
                    .paidAmount(deposit.getPrice())
                    .merchantUid(deposit.getMerchantUid())
                    .build();
            updateToConfirmAwaiting(confirmDto);
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
    public void updateToConfirmAwaiting(ConfirmDto confirmDto) {
        Deposit deposit = depositDao.findByMerchantUid(confirmDto.getMerchantUid())
                .orElseThrow();
        DepositStatusEnum targetStatus = DepositStatusEnum.CONFIRM_AWAITING;
        int paidAmount = confirmDto.getPaidAmount();

        if (deposit.getPrice() != paidAmount) {
            throw new ValidationFailedException("결제 금액 불일치");
        }

        if (!targetStatus.equals(deposit.getStatus())) {
            deposit.updateStatus(targetStatus);
            depositConfirmAwaitingProducer.produce(confirmDto);
        }
    }

    @Transactional
    public void updateToRefundAwaiting(RefundDto refundDto) {
        Deposit deposit = depositDao.findByMerchantUid(refundDto.getMerchantUid())
                .orElseThrow();
        DepositStatusEnum targetStatus = DepositStatusEnum.REFUND_AWAITING;

        if (!targetStatus.equals(deposit.getStatus())) {
            deposit.updateStatus(targetStatus);
            depositRefundAwaitingProducer.produce(refundDto);
        }
    }

    @Transactional
    public void updateToRefundAwaiting(String merchantUid) {
        updateToRefundAwaiting(toRefundDto(merchantUid));
    }

    @Transactional
    public void updateToRefundAwaiting(PaymentDto paymentDto) {
        updateToRefundAwaiting(toRefundDto(paymentDto));
    }

    @Transactional
    public void updateToConfirmed(PaymentDto paymentDto) {
        Response response = paymentDto.getResponse();
        Deposit deposit = depositDao.findByMerchantUid(response.getMerchantUid())
                .orElseThrow();
        DepositStatusEnum targetStatus = DepositStatusEnum.CONFIRMED;
        int paidAmount = response.getAmount();

        if (deposit.getPrice() != paidAmount) {
            throw new ValidationFailedException("결제 금액 불일치");
        }

        if (!targetStatus.equals(deposit.getStatus())) {
            deposit.setImpUid(response.getImpUid());
            deposit.updateStatus(targetStatus);
        }
    }

    @Transactional
    public void updateToRefunded(PaymentDto paymentDto) {
        Response response = paymentDto.getResponse();
        Deposit deposit = depositDao.findByMerchantUid(response.getMerchantUid())
                .orElseThrow();
        DepositStatusEnum targetStatus = DepositStatusEnum.REFUNDED;

        if (!targetStatus.equals(deposit.getStatus())) {
            deposit.updateStatus(targetStatus);
        }
    }

    private RefundDto toRefundDto(PaymentDto paymentDto) {
        Response response = paymentDto.getResponse();
        return RefundDto.builder()
                .impUid(response.getImpUid())
                .merchantUid(response.getMerchantUid())
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
                DepositStatusEnum.CONFIRM_AWAITING,
                DepositStatusEnum.CREATED
        );
        return depositDao.findByUserIdAndAuctionIdAndStatusIn(userId, auctionId, pendingStatuses)
                .orElse(null);
    }
}
