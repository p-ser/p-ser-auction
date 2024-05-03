package com.pser.auction.application;

import com.pser.auction.producer.AuctionProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuctionService {
    private final AuctionProducer auctionProducer;
}
