package com.pser.auction.infra.kafka.producer;

import com.pser.auction.config.kafka.KafkaTopics;
import com.pser.auction.dto.RefundDto;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DepositRefundAwaitingProducer {
    private final KafkaTemplate<String, RefundDto> refundDtoValueKafkaTemplate;

    public void notifyRefundAwaiting(RefundDto refundDto) {
        refundDtoValueKafkaTemplate.send(KafkaTopics.DEPOSIT_REFUND_AWAITING, refundDto);
    }
}
