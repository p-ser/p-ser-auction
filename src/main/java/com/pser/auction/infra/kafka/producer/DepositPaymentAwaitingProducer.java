package com.pser.auction.infra.kafka.producer;

import com.pser.auction.config.kafka.KafkaTopics;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DepositPaymentAwaitingProducer {
    private final KafkaTemplate<String, String> stringValueKafkaTemplate;

    public void notifyPaymentAwaiting(String merchantUid) {
        stringValueKafkaTemplate.send(KafkaTopics.DEPOSIT_PAYMENT_AWAITING, merchantUid);
    }
}
