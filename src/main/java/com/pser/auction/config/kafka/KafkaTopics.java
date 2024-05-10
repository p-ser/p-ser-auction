package com.pser.auction.config.kafka;

public interface KafkaTopics {
    String DEPOSIT_PAYMENT_AWAITING = "deposit.payment-awaiting";
    String DEPOSIT_CONFIRM_AWAITING = "deposit.confirm-awaiting";
    String DEPOSIT_REFUND_AWAITING = "deposit.refund-awaiting";
    String DEPOSIT_CONFIRMED = "deposit.confirmed";
    String DEPOSIT_REFUNDED = "deposit.refunded";
}
