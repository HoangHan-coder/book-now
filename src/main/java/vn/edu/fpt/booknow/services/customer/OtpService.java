package vn.edu.fpt.booknow.services.customer;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class OtpService {

    private final RedisTemplate<String, Integer> redisTemplateToSaveInt;
    private final RedisTemplate<String, Object> redisTemplateObj;
    private final JavaMailSender mailSender;

    @Autowired
    public OtpService(RedisTemplate<String, Integer> redisTemplateToSaveInt, RedisTemplate<String, Object> redisTemplateObj, JavaMailSender mailSender) {
        this.redisTemplateToSaveInt = redisTemplateToSaveInt;
        this.redisTemplateObj = redisTemplateObj;
        this.mailSender = mailSender;
    }

    private static final long OTP_EXPIRE = 1; // phút

    public void sendOtp(String email) {
        String otp = String.valueOf(100000 + new Random().nextInt(900000));
        boolean flag = redisTemplateObj.hasKey("OTP:" + email);
        redisTemplateObj.opsForValue()
                .set("OTP:" + email, otp, OTP_EXPIRE, TimeUnit.MINUTES);
        int count;
        if (flag) {
            count = 1;
        } else {
            count = 0;
        }

        redisTemplateToSaveInt.opsForValue()
                .set("OTP", count, OTP_EXPIRE, TimeUnit.MINUTES);

        sendEmail(email, otp);
    }

    public boolean verifyOtp(String email, String otp) {
        String key = "OTP:" + email;
        Object savedOtp = redisTemplateObj.opsForValue().get(key);

        if (savedOtp != null && savedOtp.toString().equals(otp)) {
            redisTemplateObj.delete(key);
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
