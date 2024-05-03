package com.pser.auction.api;

import com.pser.auction.common.response.ApiResponse;
import com.pser.auction.dto.AuctionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuctionApi {
    @GetMapping("/users/{userId}/auctions")
    @PreAuthorize("#authId == #userId")
    public ResponseEntity<ApiResponse<Page<AuctionResponse>>> getAllAuctionsByUser(
            @RequestHeader("User-Id") long authId,
            @PathVariable long userId,
            @PageableDefault Pageable pageable) {
        //TODO: 서비스, DAO 구현 필요
        //TODO: 내놓은 경매 + 참여한 경매
        Page<AuctionResponse> result = null;
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
