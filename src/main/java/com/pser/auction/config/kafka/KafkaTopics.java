package com.pser.auction.config.kafka;

public interface KafkaTopics {
    String DEPOSIT_CREATED = "deposit.created";
    String DEPOSIT_CONFIRM_AWAITING = "deposit.confirm-awaiting";
    String DEPOSIT_CONFIRM_AWAITING_ROLLBACK = "deposit.confirm-awaiting-rollback";
    String DEPOSIT_REFUND_AWAITING = "deposit.refund-awaiting";
    String DEPOSIT_CONFIRMED = "deposit.confirmed";
    String DEPOSIT_REFUNDED = "deposit.refunded";
}
