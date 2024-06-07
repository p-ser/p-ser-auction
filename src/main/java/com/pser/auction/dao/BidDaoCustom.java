package com.pser.auction.dao;

import com.pser.auction.domain.Bid;
import com.pser.auction.dto.BidSearchRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface BidDaoCustom {
    Slice<Bid> findAllByAuctionId(BidSearchRequest request, Pageable pageable);
}
