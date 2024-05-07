package com.pser.auction.application;

import com.pser.auction.dto.ConfirmDto;
import com.pser.auction.dto.PaymentDto;
import com.pser.auction.dto.RefundDto;
import com.pser.auction.producer.DepositConfirmAwaitingProducer;
import com.pser.auction.producer.DepositRefundAwaitingProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DepositService {
    private final DepositConfirmAwaitingProducer depositConfirmAwaitingProducer;
    private final DepositRefundAwaitingProducer depositRefundAwaitingProducer;

    public long save() {
        // TODO: 결제 대기 상태 보증금 생성
        return 1L;
    }

    public void updateToConfirmAwaiting(ConfirmDto confirmDto) {
        // TODO: 상태를 컨펌 대기 상태로 변경
        depositConfirmAwaitingProducer.notifyConfirmAwaiting(confirmDto);
    }

    public void updateToRefundAwaiting(RefundDto refundDto) {
        // TODO: 상태를 환불 대기 상태로 변경
        depositRefundAwaitingProducer.notifyRefundAwaiting(refundDto);
    }

    public void updateToConfirmed(PaymentDto paymentDto) {
        // TODO: 상태를 컨펌 완료 상태로 변경
    }

    public void updateToRefunded(PaymentDto paymentDto) {
        // TODO: 상태를 환불 완료 상태로 변경
    }
}
