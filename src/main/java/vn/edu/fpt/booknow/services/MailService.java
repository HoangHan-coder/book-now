package vn.edu.fpt.booknow.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class MailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendOtp(String toEmail, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Mã OTP Reset Mật Khẩu");
        message.setText("Mã OTP của bạn là: " + otp + ". Mã này sẽ hết hạn trong 5 phút.");
        mailSender.send(message);
    }
}
