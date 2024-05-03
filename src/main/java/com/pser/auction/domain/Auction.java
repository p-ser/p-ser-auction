package com.pser.auction.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Entity
@NoArgsConstructor
@ToString(of = {"price", "endPrice", "startAt", "endAt", "depositPrice", "status"})
public class Auction extends BaseEntity {
    @Column(unique = true, nullable = false)
    private long auctionedReservationId;

    @Column(unique = true, nullable = false)
    private long derivedReservationId;

    @Column(nullable = false)
    private int price;

    @Column(nullable = false)
    private int endPrice;

    @Column(nullable = false)
    private LocalDateTime startAt;

    @Column(nullable = false)
    private LocalDateTime endAt;

    @Column(nullable = false)
    private int depositPrice;

    @Column(nullable = false)
    private AuctionStatusEnum status;

    @Builder
    public Auction(long auctionedReservationId, long derivedReservationId, int price, int endPrice,
                   LocalDateTime startAt, LocalDateTime endAt, int depositPrice, AuctionStatusEnum status) {
        this.auctionedReservationId = auctionedReservationId;
        this.derivedReservationId = derivedReservationId;
        this.price = price;
        this.endPrice = endPrice;
        this.startAt = startAt;
        this.endAt = endAt;
        this.depositPrice = depositPrice;
        this.status = status;
    }

    @PrePersist
    private void validate() {
        if (price > endPrice) {
            throw new IllegalArgumentException("낙찰가가 경매 시작가보다 클 수 없습니다.");
        }
    }
}
