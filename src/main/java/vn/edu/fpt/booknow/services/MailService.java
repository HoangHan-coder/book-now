package vn.edu.fpt.booknow.services;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class MailService {

    @Autowired
    private JavaMailSender mailSender;
    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();
    private Bucket createNewBucket() {
        return Bucket.builder()
                .addLimit(Bandwidth.classic(1, Refill.intervally(1, Duration.ofMinutes(1))))
                .build();
    }
    private boolean isRateLimited(String username) {
        Bucket bucket = cache.computeIfAbsent(username, k -> createNewBucket());
        return !bucket.tryConsume(1);
    }

    public void send(String toEmail, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Mã OTP Reset Mật Khẩu");
        message.setText("Mã OTP của bạn là: " + otp + ". Mã này sẽ hết hạn trong 5 phút.");
        mailSender.send(message);
    }
    public void sendReasonFailed(String toEmail, String bookingCode, String reason) {
        try {
            if (isRateLimited(toEmail)) {
                return ;
            }
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
    public void sendNewPassword(String toEmail, String newPassword) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject("Mật khẩu mới của bạn");

            String content = """
            <div style="font-family:Arial,sans-serif;line-height:1.6">
                <h2 style="color:#3b82f6;">Cập nhật mật khẩu thành công</h2>

                <p>Xin chào,</p>

                <p>Mật khẩu của bạn đã được cập nhật thành công.</p>

                <p><b>Mật khẩu mới:</b> %s</p>

                <p>Vui lòng đăng nhập và đổi lại mật khẩu nếu cần.</p>

                <br>
                <p style="color:#6b7280">
                    Trân trọng,<br>
                    BookNow System
                </p>
            </div>
            """.formatted(newPassword);

            helper.setText(content, true);

            mailSender.send(message);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendAccountCreated(String toEmail, String fullName, String phone, String email, String password) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject("Tài khoản của bạn đã được tạo");

            String content = """
        <div style="font-family:Arial,sans-serif;line-height:1.6">
            <h2 style="color:#22c55e;">Tạo tài khoản thành công</h2>

            <p>Xin chào <b>%s</b>,</p>

            <p>Tài khoản của bạn đã được tạo thành công với thông tin:</p>

            <ul>
                <li><b>Họ tên:</b> %s</li>
                <li><b>Số điện thoại:</b> %s</li>
                <li><b>Email:</b> %s</li>
                <li><b>Mật khẩu:</b> %s</li>
            </ul>

            <p>Bạn đã có thể đăng nhập vào hệ thống Booking rồi nhé!</p>

            <br>
            <p style="color:#6b7280">
                Trân trọng,<br>
                BookNow System
            </p>
        </div>
        """.formatted(fullName, fullName, phone, email, password);

            helper.setText(content, true);

            mailSender.send(message);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
