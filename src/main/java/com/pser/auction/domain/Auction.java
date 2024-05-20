package com.pser.auction.domain;

import com.pser.auction.domain.event.StatusHolderEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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
public class Auction extends StatusHolderEntity<AuctionStatusEnum> {
    @Column(unique = true, nullable = false)
    private long reservationId;

    private Long winnerId;

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

    @OneToMany(mappedBy = "auction", cascade = {CascadeType.PERSIST,
            CascadeType.REMOVE}, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Bid> bids = new ArrayList<>();

    @OneToMany(mappedBy = "auction", cascade = {CascadeType.PERSIST,
            CascadeType.REMOVE}, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Deposit> deposits = new ArrayList<>();

    @Builder
    public Auction(long reservationId, int price, int endPrice, LocalDateTime endAt, int depositPrice) {
        this.reservationId = reservationId;
        this.price = price;
        this.endPrice = endPrice;
        this.endAt = endAt;
        this.depositPrice = depositPrice;
    }

    public void updateWinner() {
        if (!status.equals(AuctionStatusEnum.PAYMENT_AWAITING)) {
            throw new IllegalArgumentException();
        }

        Bid winnerBid = getWinnerBid();
        setEndPrice(winnerBid.getPrice());
        setWinnerId(winnerBid.getUserId());
    }

    private Bid getWinnerBid() {
        return bids.stream().max((bid1, bid2) -> {
            if (bid1.getPrice() > bid2.getPrice()) {
                return 1;
            } else if (bid1.getPrice() < bid2.getPrice()) {
                return -1;
            }
            return 0;
        }).orElseThrow();
    }

    @PrePersist
    private void validate() {
        if (price > endPrice) {
            throw new IllegalArgumentException("낙찰가가 경매 시작가보다 클 수 없습니다.");
        }
    }
}
