package vn.edu.fpt.booknow.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.UUID;

@Service
public class OTPService {

    private static final int OTP_LENGTH = 6;
    private static final long OTP_TTL_MINUTES = 5;
    private static final long RESET_TOKEN_TTL_MINUTES = 10;
    private static final int MAX_OTP_ATTEMPTS = 5;
    private static final long RESEND_COOLDOWN_SECONDS = 60;

    private final RedisService redisService;
    private final SecureRandom secureRandom;

    @Autowired
    public OTPService(RedisService redisService) {
        this.redisService = redisService;
        this.secureRandom = new SecureRandom();
    }

    /**
     * Generate a 6-digit OTP and store in Redis
     */
    public String generateAndSaveOtp(String email) {
        String otp = generateOtp();
        redisService.saveOtp(email, otp, OTP_TTL_MINUTES);
        return otp;
    }

    /**
     * Generate 6-digit numeric OTP
     */
    private String generateOtp() {
        StringBuilder otp = new StringBuilder();
        for (int i = 0; i < OTP_LENGTH; i++) {
            otp.append(secureRandom.nextInt(10));
        }
        return otp.toString();
    }

    /**
     * Validate OTP with attempt limiting
     */
    public OtpValidationResult validateOtp(String email, String inputOtp) {
        // Check if OTP exists
        if (!redisService.hasOtp(email)) {
            return OtpValidationResult.expired();
        }

        // Check attempts
        int attempts = redisService.getOtpAttempts(email);
        if (attempts >= MAX_OTP_ATTEMPTS) {
            // Too many failed attempts, delete OTP
            redisService.deleteOtp(email);
            redisService.resetOtpAttempts(email);
            return OtpValidationResult.tooManyAttempts();
        }

        String storedOtp = redisService.getOtp(email);
        if (storedOtp == null || !storedOtp.equals(inputOtp)) {
            // Increment failed attempts
            redisService.incrementOtpAttempts(email);
            return OtpValidationResult.invalid(MAX_OTP_ATTEMPTS - attempts - 1);
        }

        // OTP is valid - clear attempts and delete OTP
        redisService.resetOtpAttempts(email);
        redisService.deleteOtp(email);
        return OtpValidationResult.valid();
    }

    /**
     * Generate reset password token
     */
    public String generateResetToken(String email) {
        String token = UUID.randomUUID().toString();
        redisService.saveResetToken(token, email, RESET_TOKEN_TTL_MINUTES);
        return token;
    }

    /**
     * Validate reset token
     */
    public String validateResetToken(String token) {
        String email = redisService.getEmailByResetToken(token);
        return email;
    }

    /**
     * Invalidate reset token after password reset
     */
    public void invalidateResetToken(String token) {
        redisService.deleteResetToken(token);
    }

    /**
     * Check if resend is on cooldown
     */
    public boolean isResendOnCooldown(String email) {
        return redisService.isResendOnCooldown(email);
    }

    /**
     * Get remaining cooldown seconds
     */
    public long getResendCooldownRemaining(String email) {
        return redisService.getResendCooldownRemaining(email);
    }

    /**
     * Set resend cooldown
     */
    public void setResendCooldown(String email) {
        redisService.setResendCooldown(email, RESEND_COOLDOWN_SECONDS);
    }

    /**
     * Check if OTP exists for email
     */
    public boolean hasOtp(String email) {
        return redisService.hasOtp(email);
    }

    // Inner class for validation result
    public static class OtpValidationResult {
        private final boolean valid;
        private final String message;
        private final int remainingAttempts;

        private OtpValidationResult(boolean valid, String message, int remainingAttempts) {
            this.valid = valid;
            this.message = message;
            this.remainingAttempts = remainingAttempts;
        }

        public static OtpValidationResult valid() {
            return new OtpValidationResult(true, "OTP hợp lệ", 0);
        }

        public static OtpValidationResult invalid(int remainingAttempts) {
            return new OtpValidationResult(false, 
                "Mã OTP không đúng. Còn " + remainingAttempts + " lần thử.", 
                remainingAttempts);
        }

        public static OtpValidationResult expired() {
            return new OtpValidationResult(false, "Mã OTP đã hết hạn hoặc không tồn tại.", 0);
        }

        public static OtpValidationResult tooManyAttempts() {
            return new OtpValidationResult(false, 
                "Bạn đã nhập sai quá nhiều lần. Vui lòng yêu cầu mã OTP mới.", 0);
        }

        public boolean isValid() {
            return valid;
        }

        public String getMessage() {
            return message;
        }

        public int getRemainingAttempts() {
            return remainingAttempts;
        }
    }
}
