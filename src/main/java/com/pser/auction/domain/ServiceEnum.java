package com.pser.auction.domain;

import lombok.Getter;

@Getter
public enum ServiceEnum {
    RESERVATION("reservation"),
    DEPOSIT("deposit");

    private final String topicPrefix;

    ServiceEnum(String topicPrefix) {
        this.topicPrefix = topicPrefix;
    }
}
