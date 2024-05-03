package com.pser.auction.dto;

import com.pser.auction.domain.AuctionStatusEnum;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

@Getter
@Setter
public class AuctionSearchRequest extends SearchQuery {
    private Long id;

    private Long auctionedReservationId;

    private Long derivedReservationId;

    private Integer priceGt;

    private Integer priceLt;

    private Integer endPriceGt;

    private Integer endPriceLt;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime startAtBefore;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime startAtAfter;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime endAtBefore;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime endAtAfter;

    private Integer depositPriceGt;

    private Integer depositPriceLt;

    private AuctionStatusEnum status;

    private String hotelName;

    private String hotelCategory;

    private String province;

    private String city;

    private String district;

    private String detailedAddress;

    @Builder
    public AuctionSearchRequest(String keyword, LocalDateTime createdAfter, LocalDateTime createdBefore,
                                LocalDateTime updatedAfter, LocalDateTime updatedBefore, Long id,
                                Long auctionedReservationId, Long derivedReservationId, Integer priceGt,
                                Integer priceLt,
                                Integer endPriceGt, Integer endPriceLt, LocalDateTime startAtBefore,
                                LocalDateTime startAtAfter, LocalDateTime endAtBefore, LocalDateTime endAtAfter,
                                Integer depositPriceGt, Integer depositPriceLt, AuctionStatusEnum status,
                                String hotelName,
                                String hotelCategory, String province, String city, String district,
                                String detailedAddress) {
        super(keyword, createdAfter, createdBefore, updatedAfter, updatedBefore);
        this.id = id;
        this.auctionedReservationId = auctionedReservationId;
        this.derivedReservationId = derivedReservationId;
        this.priceGt = priceGt;
        this.priceLt = priceLt;
        this.endPriceGt = endPriceGt;
        this.endPriceLt = endPriceLt;
        this.startAtBefore = startAtBefore;
        this.startAtAfter = startAtAfter;
        this.endAtBefore = endAtBefore;
        this.endAtAfter = endAtAfter;
        this.depositPriceGt = depositPriceGt;
        this.depositPriceLt = depositPriceLt;
        this.status = status;
        this.hotelName = hotelName;
        this.hotelCategory = hotelCategory;
        this.province = province;
        this.city = city;
        this.district = district;
        this.detailedAddress = detailedAddress;
    }
}
