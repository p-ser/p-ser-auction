package com.pser.auction.consumer;

import com.pser.auction.application.AuctionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DepositRefundedConsumer {
    private final AuctionService auctionService;
}
