package com.pser.auction.dto;

import com.pser.auction.domain.Auction;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class DepositCreateRequest {
    @NotNull
    private long userId;

    @NotNull
    private long auctionId;

    private Auction auction;
}
