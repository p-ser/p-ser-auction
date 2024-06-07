package com.pser.auction.api;

import com.pser.auction.application.BidService;
import com.pser.auction.common.response.ApiResponse;
import com.pser.auction.dto.BidCreateRequest;
import com.pser.auction.dto.BidResponse;
import com.pser.auction.dto.BidSearchRequest;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auctions/{auctionId}/bids")
public class BidApi {
    private final BidService bidService;

    @GetMapping
    public ResponseEntity<ApiResponse<Slice<BidResponse>>> getAllByAuctionId(@PathVariable long auctionId,
                                                                             BidSearchRequest request,
                                                                             @PageableDefault Pageable pageable) {
        request.setAuctionId(auctionId);
        Slice<BidResponse> result = bidService.getAllByAuctionId(request, pageable);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> save(@PathVariable long auctionId,
                                                  @Validated @RequestBody BidCreateRequest request) {
        request.setAuctionId(auctionId);
        long id = bidService.save(request);
        URI uri = URI.create("/auctions/%d/bids/%d".formatted(auctionId, id));
        return ResponseEntity.created(uri)
                .build();
    }
}

