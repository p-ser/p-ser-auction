package com.pser.auction.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.ConstraintMode;
import jakarta.persistence.Entity;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.Optional;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = {"user_id", "auction_id"}
                )
        }
)
public class Deposit extends BaseEntity {
    @Column(nullable = false)
    private long user_id;

    @ManyToOne(cascade = {CascadeType.PERSIST}, optional = false)
    @JoinColumn(nullable = false, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Auction auction;

    @Column(nullable = false)
    private String merchantUid;

    @Column(nullable = false)
    private String impUid;

    @Column(nullable = false)
    private DepositStatusEnum status = DepositStatusEnum.PAYMENT_AWAITING;

    @Builder
    public Deposit(long user_id, Auction auction, String merchantUid, String impUid, DepositStatusEnum status) {
        this.user_id = user_id;
        this.auction = auction;
        this.merchantUid = merchantUid;
        this.impUid = impUid;
        this.status = Optional.ofNullable(status).orElse(this.status);
    }
}
