package com.pser.auction.infra.kafka.producer;

import com.pser.auction.config.kafka.KafkaTopics;
import com.pser.auction.dto.ConfirmDto;
import com.pser.auction.dto.RefundDto;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DepositStatusProducer {
    private final KafkaTemplate<String, String> stringValueKafkaTemplate;
    private final KafkaTemplate<String, ConfirmDto> confirmDtoValueKafkaTemplate;
    private final KafkaTemplate<String, RefundDto> refundDtoValueKafkaTemplate;

    public void produceCreated(String merchantUid) {
        stringValueKafkaTemplate.send(KafkaTopics.DEPOSIT_CREATED, merchantUid);
    }

    public void produceConfirmAwaiting(ConfirmDto confirmDto) {
        confirmDtoValueKafkaTemplate.send(KafkaTopics.DEPOSIT_CONFIRM_AWAITING, confirmDto);
    }

    public void produceRefundAwaiting(RefundDto refundDto) {
        refundDtoValueKafkaTemplate.send(KafkaTopics.DEPOSIT_REFUND_AWAITING, refundDto);
    }
}
