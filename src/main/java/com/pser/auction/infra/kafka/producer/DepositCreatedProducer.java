package com.pser.auction.infra.kafka.producer;

import com.pser.auction.config.kafka.KafkaTopics;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DepositCreatedProducer {
    private final KafkaTemplate<String, String> stringValueKafkaTemplate;

    public void notifyCreated(String merchantUid) {
        stringValueKafkaTemplate.send(KafkaTopics.DEPOSIT_CREATED, merchantUid);
    }
}
