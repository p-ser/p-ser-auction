package com.pser.auction.exception;

public class StatusUpdateException extends RuntimeException {
    public StatusUpdateException() {
        this("업데이트할 수 없는 상태입니다");
    }

    public StatusUpdateException(String message) {
        super(message);
    }
}
