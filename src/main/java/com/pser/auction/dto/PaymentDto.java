package com.pser.auction.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
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
public class PaymentDto {
    private int code;
    private String message;
    private Response response;

    @Getter
    @Setter
    public static class Response {
        @JsonAlias("amount")
        private Integer amount;

        @JsonAlias("apply_num")
        private String applyNum;

        @JsonAlias("bank_code")
        private String bankCode;

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

        @JsonAlias("cancel_amount")
        private Integer cancelAmount;

        @JsonAlias("cancel_reason")
        private String cancelReason;

        @JsonAlias("cancelled_at")
        private Integer cancelledAt;

        @JsonAlias("card_code")
        private String cardCode;

        @JsonAlias("card_name")
        private String cardName;

        @JsonAlias("card_number")
        private String cardNumber;

        @JsonAlias("card_quota")
        private Integer cardQuota;

        @JsonAlias("card_type")
        private Integer cardType;

        @JsonAlias("cash_receipt_issued")
        private Boolean cashReceiptIssued;

        @JsonAlias("channel")
        private String channel;

        @JsonAlias("currency")
        private String currency;

        @JsonAlias("custom_data")
        private String customData;

        @JsonAlias("customer_uid")
        private String customerUid;

        @JsonAlias("customer_uid_usage")
        private String customerUidUsage;

        @JsonAlias("emb_pg_provider")
        private String embPgProvider;

        @JsonAlias("escrow")
        private Boolean escrow;

        @JsonAlias("fail_reason")
        private String failReason;

        @JsonAlias("failed_at")
        private Integer failedAt;

        @JsonAlias("imp_uid")
        private String impUid;

        @JsonAlias("merchant_uid")
        private String merchantUid;

        @JsonAlias("name")
        private String name;

        @JsonAlias("paid_at")
        private Integer paidAt;

        @JsonAlias("pay_method")
        private String payMethod;

        @JsonAlias("pg_id")
        private String pgId;

        @JsonAlias("pg_provider")
        private String pgProvider;

        @JsonAlias("pg_tid")
        private String pgTid;

        @JsonAlias("receipt_url")
        private String receiptUrl;

        @JsonAlias("started_at")
        private Integer startedAt;

        @JsonAlias("status")
        private String status;

        @JsonAlias("user_agent")
        private String userAgent;

        @JsonAlias("vbank_code")
        private String vbankCode;

        @JsonAlias("vbank_date")
        private Integer vbankDate;

        @JsonAlias("vbank_holder")
        private String vbankHolder;

        @JsonAlias("vbank_issued_at")
        private Integer vbankIssuedAt;

        @JsonAlias("vbank_name")
        private String vbankName;

        @JsonAlias("vbank_num")
        private String vbankNum;
    }
}
