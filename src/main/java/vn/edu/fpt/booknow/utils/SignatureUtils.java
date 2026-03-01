package vn.edu.fpt.booknow.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Component
public class SignatureUtils {

    private static final Logger log = LoggerFactory.getLogger(SignatureUtils.class);
    private static final String HMAC_SHA256_ALGORITHM = "HmacSHA256";

    public String generateSignature(String rawData, String secretKey) {
        try {
            SecretKeySpec secretKeySpec = new SecretKeySpec(
                secretKey.getBytes(StandardCharsets.UTF_8),
                HMAC_SHA256_ALGORITHM
            );
            Mac mac = Mac.getInstance(HMAC_SHA256_ALGORITHM);
            mac.init(secretKeySpec);
            byte[] signatureBytes = mac.doFinal(rawData.getBytes(StandardCharsets.UTF_8));

            StringBuilder hexString = new StringBuilder();
            for (byte b : signatureBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            log.debug("Raw data: {}", rawData);
            log.debug("Signature: {}", hexString);
            return hexString.toString();

        } catch (Exception e) {
            log.error("Lỗi tạo signature HMAC SHA256: {}", e.getMessage(), e);
            throw new RuntimeException("Không thể tạo signature", e);
        }
    }

    public boolean verifySignature(String rawData, String receivedSignature, String secretKey) {
        String expectedSignature = generateSignature(rawData, secretKey);
        return MessageDigest.isEqual(
            expectedSignature.getBytes(StandardCharsets.UTF_8),
            receivedSignature.getBytes(StandardCharsets.UTF_8)
        );
    }
}
