package com.pser.auction.infra.kafka.consumer;

import com.pser.auction.application.AuctionService;
import com.pser.auction.config.kafka.KafkaTopics;
import com.pser.auction.dto.PaymentDto;
import com.pser.auction.exception.ValidationFailedException;
import io.vavr.control.Try;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuctionPaymentValidationCheckedConsumer {
    private final AuctionService auctionService;

    @RetryableTopic(kafkaTemplate = "paymentDtoValueKafkaTemplate", attempts = "5")
    @KafkaListener(topics = KafkaTopics.AUCTION_PAYMENT_VALIDATION_CHECKED, groupId = "${kafka.consumer-group-id}", containerFactory = "paymentDtoValueListenerContainerFactory")
    public void updateToPaymentValidationChecked(PaymentDto paymentDto) {
        Try.run(() -> auctionService.updateToPaid(paymentDto))
                .recover(ValidationFailedException.class, (e) -> {
                    auctionService.updateToRefundRequired(paymentDto);
                    return null;
                })
                .get();
    }

    @DltHandler
    public void dltHandler(ConsumerRecord<String, PaymentDto> record) {
        PaymentDto paymentDto = record.value();
        auctionService.updateToRefundRequired(paymentDto);
    }
}
