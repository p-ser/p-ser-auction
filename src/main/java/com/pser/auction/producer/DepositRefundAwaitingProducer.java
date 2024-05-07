package com.pser.auction.producer;

import com.pser.auction.dto.RefundDto;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DepositRefundAwaitingProducer {
    private final KafkaTemplate<String, RefundDto> refundDtoValueKafkaTemplate;

    public void notifyRefundAwaiting(RefundDto refundDto) {
        refundDtoValueKafkaTemplate.send("deposit-refund-awaiting", refundDto);
    }
}
