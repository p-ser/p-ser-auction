package com.pser.auction.exception;

public class SameStatusException extends RuntimeException {
    public SameStatusException() {
        this("이미 해당 상태에 있습니다");
    }

    public SameStatusException(String message) {
        super(message);
    }
}
