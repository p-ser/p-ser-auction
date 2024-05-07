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
import java.util.UUID;
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
    private long userId;

    @ManyToOne(cascade = {CascadeType.PERSIST}, optional = false)
    @JoinColumn(nullable = false, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Auction auction;

    @Column(nullable = false, unique = true)
    private String merchantUid = UUID.randomUUID().toString();

    private String impUid;

    @Column(nullable = false)
    private DepositStatusEnum status = DepositStatusEnum.PAYMENT_AWAITING;

    @Builder
    public Deposit(long userId, Auction auction, String impUid, DepositStatusEnum status) {
        this.userId = userId;
        this.auction = auction;
        this.impUid = impUid;
        this.status = Optional.ofNullable(status).orElse(this.status);
    }
}
