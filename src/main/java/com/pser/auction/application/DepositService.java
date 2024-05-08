package com.pser.auction.application;

import com.pser.auction.dao.AuctionDao;
import com.pser.auction.dao.DepositDao;
import com.pser.auction.domain.Auction;
import com.pser.auction.domain.Deposit;
import com.pser.auction.domain.DepositStatusEnum;
import com.pser.auction.dto.ConfirmDto;
import com.pser.auction.dto.DepositCreateRequest;
import com.pser.auction.dto.DepositMapper;
import com.pser.auction.dto.PaymentDto;
import com.pser.auction.dto.PaymentDto.Response;
import com.pser.auction.dto.RefundDto;
import com.pser.auction.producer.DepositConfirmAwaitingProducer;
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

    @Transactional
    public long save(DepositCreateRequest request) {
        Auction auction = auctionDao.findById(request.getAuctionId())
                .orElseThrow();
        Deposit deposit = depositMapper.toEntity(request, auction);
        depositDao.save(deposit);
        return deposit.getId();
    }

    @Transactional
    public void updateToConfirmAwaiting(ConfirmDto confirmDto) {
        Deposit deposit = depositDao.findByMerchantUid(confirmDto.getMerchantUid())
                .orElseThrow();
        deposit.updateStatus(DepositStatusEnum.CONFIRM_AWAITING);
        depositConfirmAwaitingProducer.notifyConfirmAwaiting(confirmDto);
    }

    @Transactional
    public void updateToRefundAwaiting(RefundDto refundDto) {
        Deposit deposit = depositDao.findByMerchantUid(refundDto.getMerchantUid())
                .orElseThrow();
        deposit.updateStatus(DepositStatusEnum.REFUND_AWAITING);
        depositRefundAwaitingProducer.notifyRefundAwaiting(refundDto);
    }

    @Transactional
    public void updateToConfirmed(PaymentDto paymentDto) {
        Response response = paymentDto.getResponse();
        Deposit deposit = depositDao.findByMerchantUid(response.getMerchantUid())
                .orElseThrow();
        deposit.updateStatus(DepositStatusEnum.CONFIRMED);
    }

    @Transactional
    public void updateToRefunded(PaymentDto paymentDto) {
        Response response = paymentDto.getResponse();
        Deposit deposit = depositDao.findByMerchantUid(response.getMerchantUid())
                .orElseThrow();
        deposit.updateStatus(DepositStatusEnum.REFUNDED);
    }
}
