package com.pser.auction.config.kafka;

public interface KafkaTopics {
    String AUCTION_CREATED = "auction.created";
    String AUCTION_CREATED_ROLLBACK = "auction.created-rollback";
    String AUCTION_PAYMENT_REQUIRED = "auction.payment-required";
    String AUCTION_PAYMENT_VALIDATION_REQUIRED = "auction.payment-validation-required";
    String AUCTION_PAYMENT_VALIDATION_CHECKED = "auction.payment-validation-checked";
    String AUCTION_PAYMENT_VALIDATION_REQUIRED_ROLLBACK = "auction.payment-validation-required-rollback";
    String AUCTION_REFUND_REQUIRED = "auction.refund-required";
    String AUCTION_REFUND_CHECKED = "auction.refund-checked";
    String AUCTION_FAILURE = "auction.failure";
    String DEPOSIT_CREATED = "deposit.created";
    String DEPOSIT_PAYMENT_VALIDATION_REQUIRED = "deposit.payment-validation-required";
    String DEPOSIT_PAYMENT_VALIDATION_REQUIRED_ROLLBACK = "deposit.payment-validation-required-rollback";
    String DEPOSIT_PAYMENT_VALIDATION_CHECKED = "deposit.payment-validation-checked";
    String DEPOSIT_REFUND_REQUIRED = "deposit.refund-required";
    String DEPOSIT_REFUND_CHECKED = "deposit.refund-checked";
}
