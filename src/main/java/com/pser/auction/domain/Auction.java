package com.pser.auction.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrePersist;
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
@ToString(of = {"price", "endPrice", "endAt", "depositPrice", "status"})
public class Auction extends BaseEntity {
    @Column(unique = true, nullable = false)
    private long reservationId;

    @Column(nullable = false)
    private int price;

    @Column(nullable = false)
    private int endPrice;

    @Column(nullable = false)
    private LocalDateTime endAt;

    @Column(nullable = false)
    private int depositPrice;

    @Column(nullable = false)
    private AuctionStatusEnum status = AuctionStatusEnum.CREATED;

    @Builder
    public Auction(long reservationId, int price, int endPrice, LocalDateTime endAt, int depositPrice) {
        this.reservationId = reservationId;
        this.price = price;
        this.endPrice = endPrice;
        this.endAt = endAt;
        this.depositPrice = depositPrice;
    }

    @PrePersist
    private void validate() {
        if (price > endPrice) {
            throw new IllegalArgumentException("낙찰가가 경매 시작가보다 클 수 없습니다.");
        }
    }
}
