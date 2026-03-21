package vn.edu.fpt.booknow.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MomoRequestDTO {

    @JsonProperty("partnerCode")  private String partnerCode;
    @JsonProperty("accessKey")    private String accessKey;
    @JsonProperty("requestId")    private String requestId;
    @JsonProperty("amount")       private String amount;
    @JsonProperty("orderId")      private String orderId;
    @JsonProperty("orderInfo")    private String orderInfo;
    @JsonProperty("ipnUrl")      private String ipnUrl;
    @JsonProperty("redirectUrl") private String redirectUrl;
    @JsonProperty("extraData")    private String extraData;
    @JsonProperty("requestType")  private String requestType;
    @JsonProperty("signature")    private String signature;
    @JsonProperty("expireDate") private String expireDate;

    private MomoRequestDTO() {}

    public String getPartnerCode() { return partnerCode; }
    public String getAccessKey()   { return accessKey; }
    public String getRequestId()   { return requestId; }
    public String getAmount()      { return amount; }
    public String getOrderId()     { return orderId; }
    public String getOrderInfo()   { return orderInfo; }
    public String getIpnUrl()   { return ipnUrl; }
    public String getRedirectUrl()   { return redirectUrl; }
    public String getExtraData()   { return extraData; }
    public String getRequestType() { return requestType; }
    public String getSignature()   { return signature; }

    public String getExpireDate() {
        return expireDate;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final MomoRequestDTO obj = new MomoRequestDTO();

        public Builder partnerCode(String v)  { obj.partnerCode  = v; return this; }
        public Builder accessKey(String v)    { obj.accessKey    = v; return this; }
        public Builder requestId(String v)    { obj.requestId    = v; return this; }
        public Builder amount(String v)       { obj.amount       = v; return this; }
        public Builder orderId(String v)      { obj.orderId      = v; return this; }
        public Builder orderInfo(String v)    { obj.orderInfo    = v; return this; }
        public Builder ipnUrl(String v)    { obj.ipnUrl    = v; return this; }
        public Builder redirectUrl(String v)    { obj.redirectUrl    = v; return this; }
        public Builder extraData(String v)    { obj.extraData    = v; return this; }
        public Builder requestType(String v)  { obj.requestType  = v; return this; }
        public Builder signature(String v)    { obj.signature    = v; return this; }
        public Builder expireDate(String v)    { obj.expireDate    = v; return this; }

        public MomoRequestDTO build()         { return obj; }
    }
}
