package com.pser.auction.dto;

import com.pser.auction.domain.Bid;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants.ComponentModel;

@Mapper(componentModel = ComponentModel.SPRING)
public interface BidMapper {
    Bid toEntity(BidCreateRequest request);

    @Mapping(source = "auction.id", target = "auctionId")
    BidResponse toResponse(Bid bid);
}
