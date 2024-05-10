package com.pser.auction.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.pser.auction.domain.DepositStatusEnum;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class DepositResponse {
    private Long id;

    private Long auctionId;

    private Long userId;

    private String merchantUid;

    private String impUid;

    private DepositStatusEnum status;

    private Long price;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime createdAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime updatedAt;
}
