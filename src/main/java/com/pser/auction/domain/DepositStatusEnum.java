package com.pser.auction.domain;

import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Getter;

@Getter
public enum DepositStatusEnum {
    PAYMENT_AWAITING(0),
    CONFIRM_AWAITING(1),
    CONFIRMED(2),
    REFUND_AWAITING(3),
    REFUNDED(4);

    private static final Map<Integer, DepositStatusEnum> valueToName =
            Collections.unmodifiableMap(Stream.of(values())
                    .collect(Collectors.toMap(DepositStatusEnum::getValue, Function.identity())));
    private final int value;

    DepositStatusEnum(int value) {
        this.value = value;
    }

    public static DepositStatusEnum getByValue(int value) {
        return valueToName.get(value);
    }
}
