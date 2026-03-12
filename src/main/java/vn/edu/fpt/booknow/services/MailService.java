package vn.edu.fpt.booknow.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class MailService {

    @Autowired
    private JavaMailSender mailSender;

    public void send(String toEmail, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Mã OTP Reset Mật Khẩu");
        message.setText("Mã OTP của bạn là: " + otp + ". Mã này sẽ hết hạn trong 5 phút.");
        mailSender.send(message);
    }
    public void sendReasonFailed(String toEmail, String bookingCode, String reason) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject("Thông báo hủy đơn booking");

            String content = """
                <div style="font-family:Arial,sans-serif;line-height:1.6">
                    <h2 style="color:#ef4444;">Thông báo hủy booking</h2>
                    
                    <p>Xin chào quý khách,</p>

                    <p>Mã booking của bạn: <b>%s</b></p>

                    <p>Đơn đặt phòng đã bị <b>hủy</b>.</p>

                    <p><b>Lí do:</b> %s</p>

                    <p>Quý khách vui lòng đặt lại booking mới trên hệ thống.</p>

                    <br>
                    <p style="color:#6b7280">
                        Trân trọng,<br>
                        BookNow Homestay
                    </p>
                </div>
                """.formatted(bookingCode, reason);

            helper.setText(content, true);

            mailSender.send(message);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void sendReasonReject(String toEmail, String bookingCode, String reason) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject("Thông báo từ chối check-in");

            String content = """
                <div style="font-family:Arial,sans-serif;line-height:1.6">
                    <h2 style="color:#f59e0b;">Thông báo từ chối check-in</h2>

                    <p>Xin chào quý khách,</p>

                    <p>Mã booking của bạn: <b>%s</b></p>

                    <p>Yêu cầu check-in đã bị <b>từ chối</b>.</p>

                    <p><b>Lí do:</b> %s</p>

                    <p>Quý khách vui lòng cập nhật lại thông tin để tiếp tục đặt phòng.</p>

                    <br>
                    <p style="color:#6b7280">
                        Trân trọng,<br>
                        BookNow Homestay
                    </p>
                </div>
                """.formatted(bookingCode, reason);

            helper.setText(content, true);

            mailSender.send(message);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
