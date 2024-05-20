package com.pser.auction.infra.kafka.consumer;

import com.pser.auction.application.AuctionService;
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
public class AuctionRefundCheckedConsumer {
    private final AuctionService auctionService;

    @RetryableTopic(kafkaTemplate = "paymentDtoValueKafkaTemplate", attempts = "5")
    @KafkaListener(topics = KafkaTopics.AUCTION_REFUND_CHECKED, groupId = "${kafka.consumer-group-id}", containerFactory = "paymentDtoValueListenerContainerFactory")
    public void updateToRefunded(PaymentDto paymentDto) {
        auctionService.updateToPaymentRequired(paymentDto);
    }
}
