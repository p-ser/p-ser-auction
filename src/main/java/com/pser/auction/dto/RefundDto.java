package com.pser.auction.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefundDto {
    @JsonAlias({"imp_uid", "impUid"})
    private String impUid;

    @JsonAlias({"merchant_uid", "merchantUid"})
    private String merchantUid;

    @JsonAlias("amount")
    private Integer amount;

    @JsonAlias({"tax_free", "taxFree"})
    private Integer taxFree;

    @JsonAlias({"vat_amount", "vatAmount"})
    private Integer vatAmount;

    @JsonAlias("checksum")
    private Integer checksum;

    @JsonAlias("reason")
    private String reason;

    @JsonAlias({"refund_holder", "refundHolder"})
    private String refundHolder;

    @JsonAlias({"refund_bank", "refundBank"})
    private String refundBank;

    @JsonAlias({"refund_account", "refundAccount"})
    private String refundAccount;

    @JsonAlias({"refund_tel", "refundTel"})
    private String refundTel;


    @JsonGetter("imp_uid")
    public String getImpUid() {
        return impUid;
    }

    @JsonSetter("imp_uid")
    public void setImpUid(String impUid) {
        this.impUid = impUid;
    }

    @JsonGetter("merchant_uid")
    public String getMerchantUid() {
        return merchantUid;
    }

    @JsonSetter("merchant_uid")
    public void setMerchantUid(String merchantUid) {
        this.merchantUid = merchantUid;
    }

    @JsonGetter("amount")
    public Integer getAmount() {
        return amount;
    }

    @JsonSetter("amount")
    public void setAmount(Integer amount) {
        this.amount = amount;
    }

    @JsonGetter("tax_free")
    public Integer getTaxFree() {
        return taxFree;
    }

    @JsonSetter("tax_free")
    public void setTaxFree(Integer taxFree) {
        this.taxFree = taxFree;
    }

    @JsonGetter("vat_amount")
    public Integer getVatAmount() {
        return vatAmount;
    }

    @JsonSetter("vat_amount")
    public void setVatAmount(Integer vatAmount) {
        this.vatAmount = vatAmount;
    }

    @JsonGetter("checksum")
    public Integer getChecksum() {
        return checksum;
    }

    @JsonSetter("checksum")
    public void setChecksum(Integer checksum) {
        this.checksum = checksum;
    }

    @JsonGetter("reason")
    public String getReason() {
        return reason;
    }

    @JsonSetter("reason")
    public void setReason(String reason) {
        this.reason = reason;
    }

    @JsonGetter("refund_holder")
    public String getRefundHolder() {
        return refundHolder;
    }

    @JsonSetter("refund_holder")
    public void setRefundHolder(String refundHolder) {
        this.refundHolder = refundHolder;
    }

    @JsonGetter("refund_bank")
    public String getRefundBank() {
        return refundBank;
    }

    @JsonSetter("refund_bank")
    public void setRefundBank(String refundBank) {
        this.refundBank = refundBank;
    }

    @JsonGetter("refund_account")
    public String getRefundAccount() {
        return refundAccount;
    }

    @JsonSetter("refund_account")
    public void setRefundAccount(String refundAccount) {
        this.refundAccount = refundAccount;
    }

    @JsonGetter("refund_tel")
    public String getRefundTel() {
        return refundTel;
    }

    @JsonSetter("refund_tel")
    public void setRefundTel(String refundTel) {
        this.refundTel = refundTel;
    }
}
