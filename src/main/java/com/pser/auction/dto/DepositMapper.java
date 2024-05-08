package com.pser.auction.dto;

import com.pser.auction.domain.Auction;
import com.pser.auction.domain.Deposit;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants.ComponentModel;

@Mapper(componentModel = ComponentModel.SPRING)
public interface DepositMapper {
    Deposit toEntity(DepositCreateRequest request, Auction auction);
}
