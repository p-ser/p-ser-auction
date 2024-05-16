package com.pser.auction.dto;

import com.pser.auction.domain.Auction;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants.ComponentModel;

@Mapper(componentModel = ComponentModel.SPRING)
public interface AuctionMapper {
    Auction toEntity(AuctionCreateRequest request);

    AuctionDto toDto(Auction auction);
}
