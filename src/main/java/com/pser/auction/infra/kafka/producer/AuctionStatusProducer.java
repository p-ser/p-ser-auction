package com.pser.auction.infra.kafka.producer;

import com.pser.auction.config.kafka.KafkaTopics;
import com.pser.auction.dto.AuctionDto;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuctionStatusProducer {
    private final KafkaTemplate<String, AuctionDto> auctionDtoValueKafkaTemplate;

    public void produceCreated(AuctionDto auctionDto) {
        auctionDtoValueKafkaTemplate.send(KafkaTopics.AUCTION_CREATED, auctionDto);
    }

    public void produceCreatedRollback(AuctionDto auctionDto) {
        auctionDtoValueKafkaTemplate.send(KafkaTopics.AUCTION_CREATED_ROLLBACK, auctionDto);
    }

    public void producePaymentAwaiting(AuctionDto auctionDto) {
        auctionDtoValueKafkaTemplate.send(KafkaTopics.AUCTION_PAYMENT_AWAITING, auctionDto);
    }

    public void produceFailure(AuctionDto auctionDto) {
        auctionDtoValueKafkaTemplate.send(KafkaTopics.AUCTION_FAILURE, auctionDto);
    }
}
