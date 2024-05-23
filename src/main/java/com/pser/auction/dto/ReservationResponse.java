package com.pser.auction.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.pser.auction.domain.ReservationStatusEnum;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReservationResponse {
    private Long userId;

    private Long roomId;

    private Integer price;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate startAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate endAt;

    private Integer visitorCount;

    private Integer adultCount;

    private Integer childCount;

    private ReservationStatusEnum status;
}
