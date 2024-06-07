package com.pser.auction.dto;

import com.pser.auction.domain.Auction;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class DepositCreateRequest {
    @NotNull
    private Long userId;

    @NotNull
    private Long auctionId;

    @Schema(hidden = true)
    private Auction auction;
}
