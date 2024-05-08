package com.pser.auction.domain.event;

public interface StatusHolder<T extends StatusEnum> {
    void updateStatus(T status);
}
