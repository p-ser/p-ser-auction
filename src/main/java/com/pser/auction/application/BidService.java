package com.pser.auction.application;

import com.pser.auction.dao.AuctionDao;
import com.pser.auction.dao.BidDao;
import com.pser.auction.dao.DepositDao;
import com.pser.auction.domain.Auction;
import com.pser.auction.domain.Bid;
import com.pser.auction.domain.DepositStatusEnum;
import com.pser.auction.dto.AuctionMapper;
import com.pser.auction.dto.BidCreateRequest;
import com.pser.auction.dto.BidMapper;
import com.pser.auction.dto.BidResponse;
import com.pser.auction.dto.BidSearchRequest;
import com.pser.auction.infra.kafka.producer.AuctionStatusProducer;
import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class BidService {
    private final BidDao bidDao;
    private final DepositDao depositDao;
    private final AuctionDao auctionDao;
    private final BidMapper bidMapper;
    private final AuctionMapper auctionMapper;
    private final AuctionStatusProducer auctionStatusProducer;

    public Slice<BidResponse> getAllByAuctionId(BidSearchRequest request, Pageable pageable) {
        Slice<Bid> result = bidDao.findAllByAuctionId(request, pageable);
        return result.map(bidMapper::toResponse);
    }

    public long save(BidCreateRequest request) {
        Auction auction = auctionDao.findById(request.getAuctionId())
                .orElseThrow();
        request.setAuction(auction);

        validateDeposit(request);

        Bid bid = bidMapper.toEntity(request);
        bidDao.save(bid);
        auction.updatePrice(request.getPrice());
        auctionStatusProducer.produceUpdated(auctionMapper.toDto(auction));
        return bid.getId();
    }

    private void validateDeposit(BidCreateRequest request) {
        depositDao.findAllByUserIdAndAuctionIdAndStatus(request.getUserId(), request.getAuctionId(),
                        DepositStatusEnum.PAID).stream()
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("보증금 결제 내역이 없습니다"));
    }
}
