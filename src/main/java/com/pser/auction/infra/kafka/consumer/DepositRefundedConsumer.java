package com.pser.auction.infra.kafka.consumer;

import com.pser.auction.application.DepositService;
import com.pser.auction.config.kafka.KafkaTopics;
import com.pser.auction.dto.PaymentDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DepositRefundedConsumer {
    private final DepositService depositService;

    @RetryableTopic(kafkaTemplate = "paymentDtoValueKafkaTemplate", attempts = "5")
    @KafkaListener(topics = KafkaTopics.DEPOSIT_REFUNDED, groupId = "${kafka.consumer-group-id}", containerFactory = "paymentDtoValueListenerContainerFactory")
    public void updateToRefunded(PaymentDto paymentDto) {
        depositService.updateToRefunded(paymentDto);
    }
}
