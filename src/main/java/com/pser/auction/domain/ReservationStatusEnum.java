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
public enum ReservationStatusEnum implements StatusEnum {
    CREATED(0) {
        @Override
        public List<StatusEnum> getNext() {
            return List.of(PAYMENT_VALIDATION_REQUIRED);
        }
    }, // 결제 대기
    PAYMENT_VALIDATION_REQUIRED(1) {
        @Override
        public List<StatusEnum> getNext() {
            return List.of(BEFORE_CHECKIN);
        }
    }, // 결제 검증 대기
    BEFORE_CHECKIN(2) {
        @Override
        public List<StatusEnum> getNext() {
            return List.of(PAST, REFUND_REQUIRED, AUCTION_ONGOING);
        }
    }, // 체크인 전
    PAST(3) {
        @Override
        public List<StatusEnum> getNext() {
            return List.of();
        }
    }, // 종료된 예약
    REFUND_REQUIRED(4) {
        @Override
        public List<StatusEnum> getNext() {
            return List.of(REFUNDED);
        }
    }, // 환불 대기
    REFUNDED(5) {
        @Override
        public List<StatusEnum> getNext() {
            return List.of();
        }
    }, // 환불됨
    AUCTION_ONGOING(6) {
        @Override
        public List<StatusEnum> getNext() {
            return List.of(AUCTION_FAILURE, BID_PAYBACK_REQUIRED);
        }
    }, // 경매 중
    BID_PAYBACK_REQUIRED(7) {
        @Override
        public List<StatusEnum> getNext() {
            return List.of(DEPOSIT_PAYBACK_REQUIRED, AUCTION_SUCCESS);
        }
    }, // 낙찰금 지급 대기
    DEPOSIT_PAYBACK_REQUIRED(8) {
        @Override
        public List<StatusEnum> getNext() {
            return List.of(AUCTION_FAILURE);
        }
    }, // 낙찰금 지급 거부 이후 보증금 지급 대기
    AUCTION_FAILURE(9) {
        @Override
        public List<StatusEnum> getNext() {
            return List.of(PAST);
        }
    }, // 경매 실패 (유찰, 낙찰금 지불 거부)
    AUCTION_SUCCESS(10) {
        @Override
        public List<StatusEnum> getNext() {
            return List.of();
        }
    }; // 경매 성공

    private static final Map<Integer, ReservationStatusEnum> valueToName =
            Collections.unmodifiableMap(Stream.of(values())
                    .collect(Collectors.toMap(ReservationStatusEnum::getValue, Function.identity())));

    private final Integer value;

    ReservationStatusEnum(Integer value) {
        this.value = value;
    }

    public static ReservationStatusEnum getByValue(Integer value) {
        return valueToName.get(value);
    }
}