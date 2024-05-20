package com.pser.auction.dao;

import com.pser.auction.domain.Auction;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuctionDao extends JpaRepository<Auction, Long> {
    Optional<Auction> findAuctionByReservationId(Long reservationId);

    Optional<Auction> findByMerchantUid(String merchantUid);
}
