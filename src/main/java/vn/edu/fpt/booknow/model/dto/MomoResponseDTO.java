package vn.edu.fpt.booknow.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class MomoResponseDTO {

    @JsonProperty("partnerCode")  private String partnerCode;
    @JsonProperty("accessKey")    private String accessKey;
    @JsonProperty("requestId")    private String requestId;
    @JsonProperty("amount")       private String amount;
    @JsonProperty("orderId")      private String orderId;
    @JsonProperty("responseTime") private String responseTime;
    @JsonProperty("message")      private String message;
    @JsonProperty("localMessage") private String localMessage;

    @JsonProperty("resultCode")   private Integer resultCode;
    @JsonProperty("payUrl")       private String payUrl;
    @JsonProperty("deeplink")     private String deeplink;
    @JsonProperty("qrCodeUrl")    private String qrCodeUrl;
    @JsonProperty("signature")    private String signature;

    public boolean isSuccess() { return resultCode != null && resultCode == 0; }
}
