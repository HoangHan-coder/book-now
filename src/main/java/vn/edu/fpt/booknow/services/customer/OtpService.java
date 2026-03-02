package vn.edu.fpt.booknow.services.customer;

import lombok.RequiredArgsConstructor;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class OtpService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final JavaMailSender mailSender;

    private static final long OTP_EXPIRE = 1; // phút

    public void sendOtp(String email) {
        String otp = String.valueOf(100000 + new Random().nextInt(900000));

        redisTemplate.opsForValue()
                .set("OTP:" + email, otp, OTP_EXPIRE, TimeUnit.MINUTES);

        sendEmail(email, otp);
    }

    public boolean verifyOtp(String email, String otp) {
        String key = "OTP:" + email;
        Object savedOtp = redisTemplate.opsForValue().get(key);

        if (savedOtp != null && savedOtp.toString().equals(otp)) {
            redisTemplate.delete(key);
            return true;
        }
        return false;
    }

    private void sendEmail(String to, String otp) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(to);
        msg.setSubject("Your OTP Code");
        msg.setText("Your OTP is: " + otp + "\nExpires in 5 minutes");
        mailSender.send(msg);
    }
}
