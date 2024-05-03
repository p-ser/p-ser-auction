package com.pser.auction.api;

import com.pser.auction.common.response.ApiResponse;
import com.pser.auction.dto.AuctionCreateRequest;
import com.pser.auction.dto.AuctionResponse;
import com.pser.auction.dto.AuctionSearchRequest;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auctions")
@RequiredArgsConstructor
public class AuctionRestApi {
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<AuctionResponse>>> search(AuctionSearchRequest request,
                                                                     @PageableDefault Pageable pageable) {
        //TODO: 서비스, DAO 구현 필요
        Page<AuctionResponse> result = null;
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/{auctionId}")
    public ResponseEntity<ApiResponse<AuctionResponse>> getById(@PathVariable long auctionId) {
        //TODO: 서비스, DAO 구현 필요
        AuctionResponse result = null;
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PostMapping
    //TODO: Preauthorize Custom Method 를 구현하여 경매의 원래 예약자가 인증 유저와 일치하는 지 검증할 것
    public ResponseEntity<Void> save(@RequestHeader("User-Id") long authId,
                                     @Validated @RequestBody AuctionCreateRequest request) {
        //TODO: 서비스, DAO 구현 필요
        long id = 1L;
        return ResponseEntity.created(URI.create("/auctions/" + id)).build();
    }

    @DeleteMapping("/{auctionId}")
    //TODO: Preauthorize Custom Method 를 구현하여 경매의 원래 예약자가 인증 유저와 일치하는 지 검증할 것
    public ResponseEntity<Void> delete(@RequestHeader("User-Id") long authId, @PathVariable long auctionId) {
        return ResponseEntity.noContent().build();
    }
}
