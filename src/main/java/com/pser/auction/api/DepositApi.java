package com.pser.auction.api;

import com.pser.auction.application.DepositService;
import com.pser.auction.common.response.ApiResponse;
import com.pser.auction.domain.DepositStatusEnum;
import com.pser.auction.dto.DepositCreateRequest;
import com.pser.auction.dto.DepositResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auctions/{auctionId}")
public class DepositApi {
    private final DepositService depositService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<DepositResponse>>> getAllByAuctionId(@PathVariable long auctionId,
                                                                                @PageableDefault Pageable pageable) {
        Page<DepositResponse> result = depositService.getAllByAuctionId(auctionId, pageable);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PostMapping("/deposits")
    public ResponseEntity<ApiResponse<DepositResponse>> save(@PathVariable long auctionId,
                                                             @RequestBody DepositCreateRequest request) {
        request.setAuctionId(auctionId);
        DepositResponse response = depositService.getOrSave(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/deposits/{depositId}/check")
    public ResponseEntity<ApiResponse<DepositStatusEnum>> checkStatus(@PathVariable long auctionId,
                                                                      @PathVariable long depositId,
                                                                      @RequestBody String impUid) {
        DepositStatusEnum response = depositService.checkStatus(depositId, impUid);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}

