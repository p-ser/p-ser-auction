package com.pser.auction.infra.kafka.producer;

import com.pser.auction.config.kafka.KafkaTopics;
import com.pser.auction.dto.ConfirmDto;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DepositConfirmAwaitingProducer {
    private final KafkaTemplate<String, ConfirmDto> confirmDtoValueKafkaTemplate;

    public void produce(ConfirmDto confirmDto) {
        confirmDtoValueKafkaTemplate.send(KafkaTopics.DEPOSIT_CONFIRM_AWAITING, confirmDto);
    }
}
