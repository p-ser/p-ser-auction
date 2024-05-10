package com.pser.auction.domain;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class DepositStatusEnumConverter implements AttributeConverter<DepositStatusEnum, Integer> {
    @Override
    public Integer convertToDatabaseColumn(DepositStatusEnum depositStatusEnum) {
        return depositStatusEnum.getValue();
    }

    @Override
    public DepositStatusEnum convertToEntityAttribute(Integer value) {
        return DepositStatusEnum.getByValue(value);
    }
}
