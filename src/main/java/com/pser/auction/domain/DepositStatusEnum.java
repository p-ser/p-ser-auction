package com.pser.auction.domain;

import com.pser.auction.domain.event.StatusEnum;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Getter;

@Getter
public enum DepositStatusEnum implements StatusEnum {
    CREATED(0) {
        @Override
        public List<StatusEnum> getNext() {
            return List.of(PAYMENT_VALIDATION_REQUIRED, REFUND_REQUIRED);
        }
    },
    PAYMENT_VALIDATION_REQUIRED(1) {
        @Override
        public List<StatusEnum> getNext() {
            return List.of(PAID, REFUND_REQUIRED);
        }
    },
    PAID(2) {
        @Override
        public List<StatusEnum> getNext() {
            return List.of(REFUND_REQUIRED);
        }
    },
    REFUND_REQUIRED(3) {
        @Override
        public List<StatusEnum> getNext() {
            return List.of(REFUNDED);
        }
    },
    REFUNDED(4) {
        @Override
        public List<StatusEnum> getNext() {
            return null;
        }
    };

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
