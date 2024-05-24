package com.pser.auction.dto;

import com.pser.auction.domain.event.StatusEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatusUpdateDto<T extends StatusEnum> {
    private Long id;

    private String merchantUid;

    private T targetStatus;
}
