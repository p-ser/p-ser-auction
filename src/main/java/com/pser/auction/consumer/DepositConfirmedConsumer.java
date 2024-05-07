package com.pser.auction.consumer;

import com.pser.auction.application.DepositService;
import com.pser.auction.config.kafka.KafkaTopics;
import com.pser.auction.dto.PaymentDto;
import com.pser.auction.dto.PaymentDto.Response;
import com.pser.auction.dto.RefundDto;
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
public class DepositConfirmedConsumer {
    private final DepositService depositService;

    @RetryableTopic(kafkaTemplate = "paymentDtoValueKafkaTemplate", attempts = "5")
    @KafkaListener(topics = KafkaTopics.DEPOSIT_CONFIRMED, groupId = "${kafka.consumer-group-id}", containerFactory = "paymentDtoValueListenerContainerFactory")
    public void updateToConfirmed(PaymentDto paymentDto) {
        depositService.updateToConfirmed(paymentDto);
    }

    @DltHandler
    public void dltHandler(ConsumerRecord<String, PaymentDto> record) {
        PaymentDto paymentDto = record.value();
        Response response = paymentDto.getResponse();
        RefundDto refundDto = RefundDto.builder()
                .impUid(response.getImpUid())
                .merchantUid(response.getMerchantUid())
                .build();
        depositService.updateToRefundAwaiting(refundDto);
    }
}
