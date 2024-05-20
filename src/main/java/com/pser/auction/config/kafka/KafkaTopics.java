package com.pser.auction.config.kafka;

public interface KafkaTopics {
    String AUCTION_CREATED = "auction.created";
    String AUCTION_CREATED_ROLLBACK = "auction.created-rollback";
    String AUCTION_PAYMENT_AWAITING = "auction.payment-awaiting";
    String AUCTION_FAILURE = "auction.failure";
    String DEPOSIT_CREATED = "deposit.created";
    String DEPOSIT_CONFIRM_AWAITING = "deposit.confirm-awaiting";
    String DEPOSIT_CONFIRM_AWAITING_ROLLBACK = "deposit.confirm-awaiting-rollback";
    String DEPOSIT_REFUND_AWAITING = "deposit.refund-awaiting";
    String DEPOSIT_CONFIRMED = "deposit.confirmed";
    String DEPOSIT_REFUNDED = "deposit.refunded";
}
