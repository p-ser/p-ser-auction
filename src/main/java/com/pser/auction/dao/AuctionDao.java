package com.pser.auction.dao;

import com.pser.auction.domain.Auction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuctionDao extends JpaRepository<Auction, Long> {
}
