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
import com.pser.auction.producer.DepositConfirmAwaitingProducer;
import com.pser.auction.producer.DepositPaymentAwaitingProducer;
import com.pser.auction.producer.DepositRefundAwaitingProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    private final DepositPaymentAwaitingProducer depositPaymentAwaitingProducer;

    @Transactional
    public DepositResponse getByMerchantUid(String merchantUid) {
        Deposit deposit = depositDao.findByMerchantUid(merchantUid)
                .orElseThrow();
        return depositMapper.toResponse(deposit);
    }

    @Transactional
    public long save(DepositCreateRequest request) {
        Auction auction = auctionDao.findById(request.getAuctionId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 경매입니다"));
        if (!AuctionStatusEnum.ON_GOING.equals(auction.getStatus())) {
            throw new IllegalArgumentException("진행중인 경매가 아닙니다");
        }
        request.setAuction(auction);
        Deposit deposit = depositMapper.toEntity(request);
        deposit = depositDao.save(deposit);
        depositPaymentAwaitingProducer.notifyPaymentAwaiting(deposit.getMerchantUid());
        return deposit.getId();
    }

    @Transactional
    public DepositResponse updateToConfirmAwaiting(ConfirmDto confirmDto) {
        Deposit deposit = depositDao.findByMerchantUid(confirmDto.getMerchantUid())
                .orElseThrow();
        DepositStatusEnum targetStatus = DepositStatusEnum.CONFIRM_AWAITING;
        int paidAmount = confirmDto.getPaidAmount();

        if (deposit.getPrice() != paidAmount) {
            throw new ValidationFailedException("결제 금액 불일치");
        }

        if (!targetStatus.equals(deposit.getStatus())) {
            deposit.updateStatus(targetStatus);
            depositConfirmAwaitingProducer.notifyConfirmAwaiting(confirmDto);
        }
        return depositMapper.toResponse(deposit);
    }

    @Transactional
    public DepositResponse updateToRefundAwaiting(RefundDto refundDto) {
        Deposit deposit = depositDao.findByMerchantUid(refundDto.getMerchantUid())
                .orElseThrow();
        DepositStatusEnum targetStatus = DepositStatusEnum.REFUND_AWAITING;

        if (!targetStatus.equals(deposit.getStatus())) {
            deposit.updateStatus(targetStatus);
            depositRefundAwaitingProducer.notifyRefundAwaiting(refundDto);
        }
        return depositMapper.toResponse(deposit);
    }

    @Transactional
    public DepositResponse updateToRefundAwaiting(String merchantUid) {
        return updateToRefundAwaiting(toRefundDto(merchantUid));
    }

    @Transactional
    public DepositResponse updateToRefundAwaiting(PaymentDto paymentDto) {
        return updateToRefundAwaiting(toRefundDto(paymentDto));
    }

    @Transactional
    public DepositResponse updateToConfirmed(PaymentDto paymentDto) {
        Response response = paymentDto.getResponse();
        Deposit deposit = depositDao.findByMerchantUid(response.getMerchantUid())
                .orElseThrow();
        DepositStatusEnum targetStatus = DepositStatusEnum.CONFIRMED;
        int paidAmount = response.getAmount();

        if (deposit.getPrice() != paidAmount) {
            throw new ValidationFailedException("결제 금액 불일치");
        }

        if (!targetStatus.equals(deposit.getStatus())) {
            deposit.updateStatus(targetStatus);
        }
        return depositMapper.toResponse(deposit);
    }

    @Transactional
    public DepositResponse updateToRefunded(PaymentDto paymentDto) {
        Response response = paymentDto.getResponse();
        Deposit deposit = depositDao.findByMerchantUid(response.getMerchantUid())
                .orElseThrow();
        DepositStatusEnum targetStatus = DepositStatusEnum.REFUNDED;

        if (!targetStatus.equals(deposit.getStatus())) {
            deposit.updateStatus(targetStatus);
        }
        return depositMapper.toResponse(deposit);
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
}
