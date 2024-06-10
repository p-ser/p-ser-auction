package com.pser.auction.infra.kafka.consumer;

import com.pser.auction.application.DepositService;
import com.pser.auction.config.kafka.KafkaTopics;
import com.pser.auction.domain.DepositStatusEnum;
import com.pser.auction.dto.PaymentDto;
import com.pser.auction.dto.RefundDto;
import com.pser.auction.dto.StatusUpdateDto;
import com.pser.auction.exception.SameStatusException;
import com.pser.auction.exception.ValidationFailedException;
import com.pser.auction.infra.kafka.producer.DepositStatusProducer;
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
public class DepositPaymentValidationCheckedConsumer {
    private final DepositService depositService;
    private final DepositStatusProducer depositStatusProducer;

    @RetryableTopic(kafkaTemplate = "paymentDtoValueKafkaTemplate", attempts = "5")
    @KafkaListener(topics = KafkaTopics.DEPOSIT_PAYMENT_VALIDATION_CHECKED, groupId = "${kafka.consumer-group-id}", containerFactory = "paymentDtoValueListenerContainerFactory")
    public void updateToPaymentValidationChecked(PaymentDto paymentDto) {
        Try.run(() -> check(paymentDto))
                .recover(SameStatusException.class, (e) -> null)
                .recover(ValidationFailedException.class, (e) -> refund(paymentDto))
                .get();
    }

    @DltHandler
    public void dltHandler(ConsumerRecord<String, PaymentDto> record) {
        Try.run(() -> {
                    PaymentDto paymentDto = record.value();
                    refund(paymentDto);
                }).recover(Exception.class, e -> {
                    log.error("dlt failed by error: " + e.getMessage());
                    return null;
                })
                .get();
    }

    private void check(PaymentDto paymentDto) {
        StatusUpdateDto<DepositStatusEnum> statusUpdateDto = StatusUpdateDto.<DepositStatusEnum>builder()
                .merchantUid(paymentDto.getMerchantUid())
                .targetStatus(DepositStatusEnum.PAID)
                .build();
        depositService.updateStatus(statusUpdateDto, auction -> {
            int paidAmount = paymentDto.getAmount();
            if (auction.getPrice() != paidAmount) {
                throw new ValidationFailedException("결제 금액 불일치");
            }
        });
    }

    private Void refund(PaymentDto paymentDto) {
        StatusUpdateDto<DepositStatusEnum> statusUpdateDto = StatusUpdateDto.<DepositStatusEnum>builder()
                .merchantUid(paymentDto.getMerchantUid())
                .targetStatus(DepositStatusEnum.REFUND_REQUIRED)
                .build();
        depositService.updateStatus(statusUpdateDto);

        RefundDto refundDto = RefundDto.builder()
                .impUid(paymentDto.getImpUid())
                .merchantUid(paymentDto.getMerchantUid())
                .build();
        depositStatusProducer.produceRefundRequired(refundDto);
        return null;
    }
}
