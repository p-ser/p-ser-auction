package com.pser.auction.domain;

import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Getter;

@Getter
public enum AuctionStatusEnum {
    CREATED(0),
    ON_GOING(1),
    PAYMENT_AWAITING(2),
    PAID(3),
    PAYMENT_DENIAL(4),
    FAILURE(5);

    private static final Map<Integer, AuctionStatusEnum> valueToName =
            Collections.unmodifiableMap(Stream.of(values())
                    .collect(Collectors.toMap(AuctionStatusEnum::getValue, Function.identity())));
    private final int value;

    AuctionStatusEnum(int value) {
        this.value = value;
    }

    public static AuctionStatusEnum getByValue(int value) {
        return valueToName.get(value);
    }
}
