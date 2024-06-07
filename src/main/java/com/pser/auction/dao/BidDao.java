package com.pser.auction.dao;

import com.pser.auction.domain.Bid;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BidDao extends JpaRepository<Bid, Long>, BidDaoCustom {
}
