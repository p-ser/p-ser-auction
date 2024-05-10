package com.pser.auction.dto;

import com.pser.auction.domain.Deposit;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants.ComponentModel;

@Mapper(componentModel = ComponentModel.SPRING)
public interface DepositMapper {
    @Mapping(source = "auction.depositPrice", target = "price")
    Deposit toEntity(DepositCreateRequest request);

    DepositResponse toResponse(Deposit deposit);
}
