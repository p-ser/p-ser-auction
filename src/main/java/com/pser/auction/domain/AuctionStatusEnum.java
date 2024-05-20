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
public enum AuctionStatusEnum implements StatusEnum {
    CREATED(0) {
        @Override
        public List<StatusEnum> getNext() {
            return List.of(ONGOING);
        }
    },
    ONGOING(1) {
        @Override
        public List<StatusEnum> getNext() {
            return List.of(PAYMENT_AWAITING, FAILURE);
        }
    },
    PAYMENT_AWAITING(2) {
        @Override
        public List<StatusEnum> getNext() {
            return List.of(PAID, PAYMENT_DENIAL);
        }
    },
    PAID(3) {
        @Override
        public List<StatusEnum> getNext() {
            return null;
        }
    },
    PAYMENT_DENIAL(4) {
        @Override
        public List<StatusEnum> getNext() {
            return null;
        }
    },
    FAILURE(5) {
        @Override
        public List<StatusEnum> getNext() {
            return null;
        }
    };

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
