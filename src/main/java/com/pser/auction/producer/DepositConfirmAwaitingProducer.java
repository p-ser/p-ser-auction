package com.pser.auction.producer;

import com.pser.auction.dto.ConfirmDto;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DepositConfirmAwaitingProducer {
    private final KafkaTemplate<String, ConfirmDto> confirmDtoValueKafkaTemplate;

    public void notifyConfirmAwaiting(ConfirmDto confirmDto) {
        confirmDtoValueKafkaTemplate.send("deposit-confirm-awaiting", confirmDto);
    }
}
