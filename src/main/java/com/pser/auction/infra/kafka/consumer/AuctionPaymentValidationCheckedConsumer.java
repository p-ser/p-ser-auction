package com.pser.auction.infra.kafka.consumer;

import com.pser.auction.application.AuctionService;
import com.pser.auction.config.kafka.KafkaTopics;
import com.pser.auction.domain.AuctionStatusEnum;
import com.pser.auction.dto.AuctionDto;
import com.pser.auction.dto.PaymentDto;
import com.pser.auction.dto.RefundDto;
import com.pser.auction.dto.StatusUpdateDto;
import com.pser.auction.exception.SameStatusException;
import com.pser.auction.exception.ValidationFailedException;
import com.pser.auction.infra.kafka.producer.AuctionStatusProducer;
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
    private final AuctionStatusProducer auctionStatusProducer;

    @RetryableTopic(kafkaTemplate = "paymentDtoValueKafkaTemplate", attempts = "5")
    @KafkaListener(topics = KafkaTopics.AUCTION_PAYMENT_VALIDATION_CHECKED, groupId = "${kafka.consumer-group-id}", containerFactory = "paymentDtoValueListenerContainerFactory")
    public void updateToPaymentValidationChecked(PaymentDto paymentDto) {
        Try.run(() -> check(paymentDto))
                .onSuccess(unused -> onSuccess(paymentDto))
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
        StatusUpdateDto<AuctionStatusEnum> statusUpdateDto = StatusUpdateDto.<AuctionStatusEnum>builder()
                .merchantUid(paymentDto.getMerchantUid())
                .targetStatus(AuctionStatusEnum.PAID)
                .build();
        auctionService.updateStatus(statusUpdateDto, auction -> {
            int paidAmount = paymentDto.getAmount();
            if (auction.getEndPrice() != paidAmount) {
                throw new ValidationFailedException("결제 금액 불일치");
            }
        });
    }

    private Void refund(PaymentDto paymentDto) {
        StatusUpdateDto<AuctionStatusEnum> statusUpdateDto = StatusUpdateDto.<AuctionStatusEnum>builder()
                .merchantUid(paymentDto.getMerchantUid())
                .targetStatus(AuctionStatusEnum.REFUND_REQUIRED)
                .build();
        auctionService.updateStatus(statusUpdateDto);

        RefundDto refundDto = RefundDto.builder()
                .impUid(paymentDto.getImpUid())
                .merchantUid(paymentDto.getMerchantUid())
                .build();
        auctionStatusProducer.produceRefundRequired(refundDto);
        return null;
    }

    private void onSuccess(PaymentDto paymentDto) {
        AuctionDto auctionDto = auctionService.getByMerchantUid(paymentDto.getMerchantUid());
        auctionStatusProducer.producePaid(auctionDto);
    }
}
