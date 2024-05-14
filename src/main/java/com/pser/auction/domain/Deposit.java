package com.pser.auction.domain;

import com.pser.auction.domain.event.StatusHolderEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.ConstraintMode;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@NoArgsConstructor
public class Deposit extends StatusHolderEntity<DepositStatusEnum> {
    @Column(nullable = false)
    private long userId;

    @ManyToOne(cascade = {CascadeType.PERSIST}, optional = false)
    @JoinColumn(nullable = false, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Auction auction;

    @Column(nullable = false, unique = true)
    private String merchantUid = UUID.randomUUID().toString();

    @Column(unique = true)
    private String impUid;

    @Column(nullable = false)
    private int price;

    @Column(nullable = false)
    @Convert(converter = DepositStatusEnumConverter.class)
    private DepositStatusEnum status = DepositStatusEnum.CREATED;

    @Builder
    public Deposit(long userId, Auction auction, int price) {
        this.userId = userId;
        this.auction = auction;
        this.price = price;
    }
}
