package com.pser.auction.domain;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class AuctionStatusEnumConverter implements AttributeConverter<AuctionStatusEnum, Integer> {
    @Override
    public Integer convertToDatabaseColumn(AuctionStatusEnum auctionStatusEnum) {
        return auctionStatusEnum.getValue();
    }

    @Override
    public AuctionStatusEnum convertToEntityAttribute(Integer value) {
        return AuctionStatusEnum.getByValue(value);
    }
}
