package com.pser.auction.infra.kafka.producer;

import com.pser.auction.config.kafka.KafkaTopics;
import com.pser.auction.dto.AuctionDto;
import com.pser.auction.dto.PaymentDto;
import com.pser.auction.dto.RefundDto;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuctionStatusProducer {
    private final KafkaTemplate<String, AuctionDto> auctionDtoValueKafkaTemplate;
    private final KafkaTemplate<String, PaymentDto> paymentDtoValueKafkaTemplate;
    private final KafkaTemplate<String, RefundDto> refundDtoValueKafkaTemplate;

    public void produceCreated(AuctionDto auctionDto) {
        auctionDtoValueKafkaTemplate.send(KafkaTopics.AUCTION_CREATED, auctionDto);
    }

    public void produceCreatedRollback(AuctionDto auctionDto) {
        auctionDtoValueKafkaTemplate.send(KafkaTopics.AUCTION_CREATED_ROLLBACK, auctionDto);
    }

    public void producePaymentRequired(AuctionDto auctionDto) {
        auctionDtoValueKafkaTemplate.send(KafkaTopics.AUCTION_PAYMENT_VALIDATION_REQUIRED, auctionDto);
    }

    public void producePaymentValidationRequired(PaymentDto paymentDto) {
        paymentDtoValueKafkaTemplate.send(KafkaTopics.AUCTION_PAYMENT_VALIDATION_REQUIRED, paymentDto);
    }

    public void produceRefundRequired(RefundDto refundDto) {
        refundDtoValueKafkaTemplate.send(KafkaTopics.AUCTION_REFUND_REQUIRED, refundDto);
    }

    public void produceFailure(AuctionDto auctionDto) {
        auctionDtoValueKafkaTemplate.send(KafkaTopics.AUCTION_FAILURE, auctionDto);
    }
}
