package vn.edu.fpt.booknow.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class RedisService {

    private static final String OTP_PREFIX = "OTP:";
    private static final String RESET_TOKEN_PREFIX = "RESET_TOKEN:";
    private static final String OTP_ATTEMPTS_PREFIX = "OTP_ATTEMPTS:";
    private static final String OTP_RESEND_PREFIX = "OTP_RESEND:";


    private final RedisTemplate<String, String> redisTemplateStr;

    @Autowired
    public RedisService(@Qualifier("redisTemplateStr")
                            RedisTemplate<String, String> redisTemplate) {
        this.redisTemplateStr = redisTemplate;
    }

    // OTP Operations
    public boolean saveOtp(String email, String otp, long ttlMinutes) {
        String key = OTP_PREFIX + email;
        Boolean success = redisTemplateStr.opsForValue().setIfAbsent(key, otp, ttlMinutes, TimeUnit.MINUTES);
        return Boolean.TRUE.equals(success);
    }

    public String getOtp(String email) {
        String key = OTP_PREFIX + email;
        return redisTemplateStr.opsForValue().get(key);
    }

    public void deleteOtp(String email) {
        String key = OTP_PREFIX + email;
        redisTemplateStr.delete(key);
    }

    public boolean hasOtp(String email) {
        String key = OTP_PREFIX + email;
        return redisTemplateStr.hasKey(key);
    }

    // Reset Token Operations
    public void saveResetToken(String token, String email, long ttlMinutes) {
        String key = RESET_TOKEN_PREFIX + token;
        redisTemplateStr.opsForValue().set(key, email, ttlMinutes, TimeUnit.MINUTES);
    }

    public String getEmailByResetToken(String token) {
        String key = RESET_TOKEN_PREFIX + token;
        return redisTemplateStr.opsForValue().get(key);
    }

    public void deleteResetToken(String token) {
        String key = RESET_TOKEN_PREFIX + token;
        redisTemplateStr.delete(key);
    }

    public boolean hasResetToken(String token) {
        String key = RESET_TOKEN_PREFIX + token;
        return redisTemplateStr.hasKey(key);
    }

    // OTP Attempts Operations
    public void incrementOtpAttempts(String email) {
        String key = OTP_ATTEMPTS_PREFIX + email;
        Long attempts = redisTemplateStr.opsForValue().increment(key);
        if (attempts != null && attempts == 1) {
            // Set expiry for new key (5 minutes)
            redisTemplateStr.expire(key, 5, TimeUnit.MINUTES);
        }
    }

    public int getOtpAttempts(String email) {
        String key = OTP_ATTEMPTS_PREFIX + email;
        String attempts = redisTemplateStr.opsForValue().get(key);
        return attempts != null ? Integer.parseInt(attempts) : 0;
    }

    public void resetOtpAttempts(String email) {
        String key = OTP_ATTEMPTS_PREFIX + email;
        redisTemplateStr.delete(key);
    }

    // OTP Resend Cooldown
    public Boolean setResendCooldown(String email, long ttlSeconds) {
        String key = OTP_RESEND_PREFIX + email;
        Boolean setResendCooldown = redisTemplateStr.opsForValue()
                .setIfAbsent(key, "1", ttlSeconds, TimeUnit.SECONDS);
        return Boolean.TRUE.equals(setResendCooldown);
    }

    public boolean isResendOnCooldown(String email) {
        String key = OTP_RESEND_PREFIX + email;
        return redisTemplateStr.hasKey(key);
    }

    public long getResendCooldownRemaining(String email) {
        String key = OTP_RESEND_PREFIX + email;
        return redisTemplateStr.getExpire(key, TimeUnit.SECONDS);
    }
}
