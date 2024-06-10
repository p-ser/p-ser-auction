package com.pser.auction.dto;

import com.pser.auction.domain.Auction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class DepositCreateRequest {
    private Long userId;

    private Long auctionId;

    private Auction auction;
}
