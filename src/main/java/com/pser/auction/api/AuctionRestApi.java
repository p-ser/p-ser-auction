package com.pser.auction.api;

import com.pser.auction.application.AuctionService;
import com.pser.auction.common.response.ApiResponse;
import com.pser.auction.domain.AuctionStatusEnum;
import com.pser.auction.dto.AuctionCreateRequest;
import com.pser.auction.dto.AuctionResponse;
import com.pser.auction.dto.AuctionSearchRequest;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
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
    private final AuctionService auctionService;

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<AuctionResponse>>> search(AuctionSearchRequest request,
                                                                     @PageableDefault Pageable pageable) {
        //TODO: 서비스 구현 필요, Elasticsearch 로 구현할 것
        Page<AuctionResponse> result = null;
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/{auctionId}")
    public ResponseEntity<ApiResponse<AuctionResponse>> getById(@PathVariable long auctionId) {
        //TODO: 서비스, Elasticsearch 에서 가져온 Materialized View 를 반환
        AuctionResponse result = null;
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/{auctionId}/check-payment")
    public ResponseEntity<ApiResponse<AuctionStatusEnum>> checkPayment(@PathVariable long auctionId,
                                                                       @RequestBody String impUid) {
        AuctionStatusEnum result = auctionService.checkPayment(auctionId, impUid);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PostMapping
    @PreAuthorize("@hotelClient.isReservationOwner(request.reservationId, authId)")
    public ResponseEntity<Void> save(@RequestHeader("User-Id") long authId,
                                     @Validated @RequestBody AuctionCreateRequest request) {
        //TODO: 서비스, DAO 구현 필요
        request.setAuthId(authId);
        long id = auctionService.save(request);
        return ResponseEntity.created(URI.create("/auctions/" + id)).build();
    }
}
