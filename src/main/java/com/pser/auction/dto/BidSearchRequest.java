package com.pser.auction.dto;

import java.util.Optional;
import lombok.Builder;
import lombok.Data;

@Data
public class BidSearchRequest {
    private Long auctionId;

    private Long idAfter = 0L;

    @Builder
    public BidSearchRequest(Long auctionId, Long idAfter) {
        this.auctionId = auctionId;
        this.idAfter = Optional.ofNullable(idAfter).orElse(this.idAfter);
    }
}
