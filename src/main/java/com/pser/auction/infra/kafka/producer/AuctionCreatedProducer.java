package com.pser.auction.infra.kafka.producer;

import com.pser.auction.config.kafka.KafkaTopics;
import com.pser.auction.dto.AuctionDto;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuctionCreatedProducer {
    private final KafkaTemplate<String, AuctionDto> auctionDtoValueKafkaTemplate;

    public void produce(AuctionDto auctionDto) {
        auctionDtoValueKafkaTemplate.send(KafkaTopics.AUCTION_CREATED, auctionDto);
    }
}
