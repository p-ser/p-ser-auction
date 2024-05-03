package com.pser.auction.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class AuctionCreateRequest {
    @NotNull
    private long auctionedReservationId;

    @NotNull
    @Min(0)
    private int price;

    @NotNull
    private LocalDateTime startAt;

    @NotNull
    private LocalDateTime endAt;

    @NotNull
    @Min(0)
    private int depositPrice;
}
