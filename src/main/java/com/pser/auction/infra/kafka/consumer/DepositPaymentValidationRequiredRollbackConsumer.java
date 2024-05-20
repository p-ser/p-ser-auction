package com.pser.auction.infra.kafka.consumer;

import com.pser.auction.application.DepositService;
import com.pser.auction.config.kafka.KafkaTopics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DepositPaymentValidationRequiredRollbackConsumer {
    private final DepositService depositService;

    @RetryableTopic(kafkaTemplate = "stringValueKafkaTemplate", attempts = "5")
    @KafkaListener(topics = KafkaTopics.DEPOSIT_PAYMENT_VALIDATION_REQUIRED_ROLLBACK, groupId = "${kafka.consumer-group-id}", containerFactory = "stringValueListenerContainerFactory")
    public void rollbackPaymentValidationRequired(String merchantUid) {
        depositService.rollbackToCreated(merchantUid);
    }
}