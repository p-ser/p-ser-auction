package com.pser.auction.producer;

import com.pser.auction.dto.ConfirmDto;
import com.pser.auction.dto.RefundDto;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuctionProducer {
    private final KafkaTemplate<String, ConfirmDto> confirmDtoValueKafkaTemplate;
    private final KafkaTemplate<String, RefundDto> refundDtoValueKafkaTemplate;

    public void notifyConfirmAwaiting(ConfirmDto confirmDto) {
        confirmDtoValueKafkaTemplate.send("deposit-confirm-awaiting", confirmDto);
    }

    public void notifyRefundAwaiting(RefundDto refundDto) {
        refundDtoValueKafkaTemplate.send("deposit-refund-awaiting", refundDto);
    }
}
