package com.pser.auction.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConfirmDto {
    @JsonAlias("apply_num")
    private String applyNum;

    @JsonAlias("bank_name")
    private String bankName;

    @JsonAlias("buyer_addr")
    private String buyerAddr;

    @JsonAlias("buyer_email")
    private String buyerEmail;

    @JsonAlias("buyer_name")
    private String buyerName;

    @JsonAlias("buyer_postcode")
    private String buyerPostcode;

    @JsonAlias("buyer_tel")
    private String buyerTel;

    @JsonAlias("card_name")
    private String cardName;

    @JsonAlias("card_number")
    private String cardNumber;

    @JsonAlias("card_quota")
    private Integer cardQuota;

    @JsonAlias("currency")
    private String currency;

    @JsonAlias("custom_data")
    private String customData;

    @JsonAlias("imp_uid")
    @NotBlank
    private String impUid;

    @JsonAlias("merchant_uid")
    @NotBlank
    private String merchantUid;

    @JsonAlias("name")
    private String name;

    @JsonAlias("paid_amount")
    private int paidAmount;

    @JsonAlias("paid_at")
    private int paidAt;

    @JsonAlias("pay_method")
    private String payMethod;

    @JsonAlias("pg_provider")
    private String pgProvider;

    @JsonAlias("pg_tid")
    private String pgTid;

    @JsonAlias("pg_type")
    private String pgType;

    @JsonAlias("receipt_url")
    private String receiptUrl;

    @JsonAlias("status")
    private String status;

    @JsonAlias("success")
    private boolean success;
}
