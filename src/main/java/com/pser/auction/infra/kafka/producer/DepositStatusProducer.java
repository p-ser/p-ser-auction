package com.pser.auction.infra.kafka.producer;

import com.pser.auction.config.kafka.KafkaTopics;
import com.pser.auction.dto.PaymentDto;
import com.pser.auction.dto.RefundDto;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DepositStatusProducer {
    private final KafkaTemplate<String, String> stringValueKafkaTemplate;
    private final KafkaTemplate<String, PaymentDto> paymentDtoValueKafkaTemplate;
    private final KafkaTemplate<String, RefundDto> refundDtoValueKafkaTemplate;

    public void produceCreated(String merchantUid) {
        stringValueKafkaTemplate.send(KafkaTopics.DEPOSIT_CREATED, merchantUid);
    }

    public void producePaymentValidationRequired(PaymentDto paymentDto) {
        paymentDtoValueKafkaTemplate.send(KafkaTopics.DEPOSIT_PAYMENT_VALIDATION_REQUIRED, paymentDto);
    }

    public void produceRefundRequired(RefundDto refundDto) {
        refundDtoValueKafkaTemplate.send(KafkaTopics.DEPOSIT_REFUND_REQUIRED, refundDto);
    }
}
