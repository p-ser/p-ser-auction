package com.pser.auction.dto;

import com.pser.auction.domain.Auction;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class BidCreateRequest {
    @NotNull
    private Long userId;

    @NotNull
    private Long auctionId;

    @Null
    @Schema(hidden = true)
    private Auction auction;

    @NotNull
    private Integer price;
}
