package com.pser.auction.exception;

public class NotOngoingAuctionException extends RuntimeException {
    public NotOngoingAuctionException() {
        this("진행중인 경매가 아닙니다");
    }

    public NotOngoingAuctionException(String message) {
        super(message);
    }
}
