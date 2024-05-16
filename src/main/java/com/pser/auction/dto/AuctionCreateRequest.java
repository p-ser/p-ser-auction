package com.pser.auction.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class AuctionCreateRequest {
    @NotNull
    private long reservationId;

    @Schema(hidden = true)
    @Null
    private Long authId;

    @NotNull
    @Min(0)
    private int endPrice;

    @NotNull
    @Min(0)
    private int price;

    @NotNull
    private LocalDateTime endAt;

    @NotNull
    @Min(0)
    private int depositPrice;
}
