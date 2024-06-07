package com.pser.auction.dao;

import com.pser.auction.domain.Bid;
import com.pser.auction.domain.QBid;
import com.pser.auction.dto.BidSearchRequest;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

@RequiredArgsConstructor
public class BidDaoImpl implements BidDaoCustom {
    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Slice<Bid> findAllByAuctionId(BidSearchRequest request, Pageable pageable) {
        QBid bid = QBid.bid;

        BooleanBuilder booleanBuilder = new BooleanBuilder()
                .and(bid.auction.id.eq(request.getAuctionId()))
                .and(bid.id.gt(request.getIdAfter()));

        List<Bid> bids = jpaQueryFactory.selectFrom(bid)
                .where(booleanBuilder)
                .limit(pageable.getPageSize() + 1)
                .orderBy(bid.id.desc())
                .fetch();
        boolean hasNext = bids.size() > pageable.getPageSize();
        return new SliceImpl<>(bids, pageable, hasNext);
    }
}
