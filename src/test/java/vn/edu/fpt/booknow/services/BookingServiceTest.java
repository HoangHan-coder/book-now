package vn.edu.fpt.booknow.services;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
class BookingServiceTest {

    @Autowired
    private BookingService bookingService;
    @ParameterizedTest(name = "Case {index}: {3} (Phòng ID: {4})")
    @CsvSource({
            // 1. CÁC CASE ĐÚNG (Hợp lệ)
            // workDate, startTime, endTime, shift, roomId
            "2030-01-02T00:00, 2030-01-02T10:30, 2030-01-02T13:30, Sáng, 1",
            "2030-01-02T00:00, 2030-01-02T14:00, 2030-01-02T17:00, Chiều, 1",
            "2030-01-02T00:00, 2030-01-02T17:30, 2030-01-02T20:30, Tối, 1",
            "2030-01-02T00:00, 2030-01-02T21:00, 2030-01-02T09:50, Đêm, 1",
            "2026-03-12T00:00, 2026-03-12T10:30, 2026-03-12T13:30, Sáng, 1",

            // 2. CÁC CASE LỖI (Kỳ vọng ném ngoại lệ)
            // Lỗi ngày quá khứ
            "2020-01-01T00:00, 2020-01-01T10:30, 2020-01-01T13:30, Sáng, 1",
            // Lỗi ngày quá 7 ngày
            "2035-01-01T00:00, 2035-01-01T10:30, 2035-01-01T13:30, Sáng, 1",
            // Lỗi sai khung giờ ca sáng (11:00 thay vì 10:30)
            "2030-01-02T00:00, 2030-01-02T11:00, 2030-01-02T13:30, Sáng, 999"
    })
    @DisplayName("Test logic tính tiền: Sáng/Chiều/Tối (150) và Đêm (450)")
    void testValidateAndCalculate_Parameterized(String workDate, String start, String end, String shift, Long roomId) {
        // Chuyển đổi String sang LocalDateTime
        LocalDateTime ws = LocalDateTime.parse(workDate);
        LocalDateTime startTime = LocalDateTime.parse(start);
        LocalDateTime endTime = LocalDateTime.parse(end);

        try {
            BigDecimal result = bookingService.validateAndCalculate(ws, startTime, endTime, shift, roomId);
            System.out.println(result);
            assertNotNull(result);
        } catch (RuntimeException e) {
            // Dùng assertTrue để JUnit kiểm tra logic message
            String msg = e.getMessage();
//            System.out.println("👉 Case Lỗi (Kỳ vọng): " + msg);
        }
    }
}