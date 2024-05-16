package com.pser.auction.dto;

import com.pser.auction.domain.AuctionStatusEnum;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuctionDto {
    private long id;

    private long reservationId;

    private int price;

    private int endPrice;

    private LocalDateTime endAt;

    private int depositPrice;

    private AuctionStatusEnum status;
}
