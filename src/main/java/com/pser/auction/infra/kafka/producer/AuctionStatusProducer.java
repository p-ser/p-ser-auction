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

    public void producePaymentRequired(AuctionDto auctionDto) {
        auctionDtoValueKafkaTemplate.send(KafkaTopics.AUCTION_PAYMENT_REQUIRED, auctionDto);
    }

    public void producePaymentValidationRequired(PaymentDto paymentDto) {
        paymentDtoValueKafkaTemplate.send(KafkaTopics.AUCTION_PAYMENT_VALIDATION_REQUIRED, paymentDto);
    }

    public void produceRefundRequired(RefundDto refundDto) {
        refundDtoValueKafkaTemplate.send(KafkaTopics.AUCTION_REFUND_REQUIRED, refundDto);
    }

    public void produceNoBid(AuctionDto auctionDto) {
        auctionDtoValueKafkaTemplate.send(KafkaTopics.AUCTION_NO_BID, auctionDto);
    }

    public void producePaid(AuctionDto auctionDto) {
        auctionDtoValueKafkaTemplate.send(KafkaTopics.AUCTION_PAID, auctionDto);
    }

    public void produceBidRefusal(AuctionDto auctionDto) {
        auctionDtoValueKafkaTemplate.send(KafkaTopics.AUCTION_BID_REFUSAL, auctionDto);
    }
}
