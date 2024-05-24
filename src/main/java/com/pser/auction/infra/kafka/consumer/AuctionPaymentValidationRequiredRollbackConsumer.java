package com.pser.auction.infra.kafka.consumer;

import com.pser.auction.application.AuctionService;
import com.pser.auction.config.kafka.KafkaTopics;
import com.pser.auction.domain.AuctionStatusEnum;
import com.pser.auction.dto.StatusUpdateDto;
import com.pser.auction.exception.SameStatusException;
import io.vavr.control.Try;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuctionPaymentValidationRequiredRollbackConsumer {
    private final AuctionService auctionService;

    @RetryableTopic(kafkaTemplate = "stringValueKafkaTemplate", attempts = "5")
    @KafkaListener(topics = KafkaTopics.AUCTION_PAYMENT_VALIDATION_REQUIRED_ROLLBACK, groupId = "${kafka.consumer-group-id}", containerFactory = "stringValueListenerContainerFactory")
    public void rollbackPaymentValidationRequired(String merchantUid) {
        Try.run(() -> {
                    StatusUpdateDto<AuctionStatusEnum> statusUpdateDto = StatusUpdateDto.<AuctionStatusEnum>builder()
                            .merchantUid(merchantUid)
                            .targetStatus(AuctionStatusEnum.PAYMENT_REQUIRED)
                            .build();

                    auctionService.rollbackStatus(statusUpdateDto);
                })
                .recover(SameStatusException.class, e -> null)
                .get();
    }
}
