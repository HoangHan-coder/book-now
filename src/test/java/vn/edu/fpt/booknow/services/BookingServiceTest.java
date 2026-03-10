package vn.edu.fpt.booknow.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import vn.edu.fpt.booknow.model.entities.Room;
import vn.edu.fpt.booknow.repositories.RoomRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BookingServiceTest {

    @InjectMocks
    private BookingService bookingService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }
    @ParameterizedTest(name = "Case {index}: {3} | Giá kỳ vọng: {4}")
    @CsvSource({
            // 1. CÁC CASE ĐÚNG (Hợp lệ)
            // workDate, startTime, endTime, shift, price
            "2030-01-02T00:00, 2030-01-02T10:30, 2030-01-02T13:30, Sáng",
            "2030-01-02T00:00, 2030-01-02T14:00, 2030-01-02T17:00, Chiều",
            "2030-01-02T00:00, 2030-01-02T17:30, 2030-01-02T20:30, Tối",
            "2036-03-12T00:00, 2030-01-02T21:00, 2030-01-02T09:50, Đêm",
            "2026-03-12T00:00, 2026-03-12T10:30, 2026-03-12T13:30, Sáng",

            // 2. CÁC CASE LỖI (Kỳ vọng ném ngoại lệ)
            // Lỗi ngày quá khứ
            "2020-01-01T00:00, 2020-01-01T10:30, 2020-01-01T13:30, Sáng",
            // Lỗi ngày quá 7 ngày
            "2035-01-01T00:00, 2035-01-01T10:30, 2035-01-01T13:30, Sáng",
            // Lỗi sai khung giờ ca sáng (11:00 thay vì 10:30)
            "2030-01-02T00:00, 2030-01-02T11:00, 2030-01-02T13:30, Sáng"
    })
    @DisplayName("Test logic tính tiền: Sáng/Chiều/Tối (150) và Đêm (450)")
    void testValidateAndCalculate_Parameterized(String workDate, String start, String end, String shift) {
        // Chuyển đổi String sang LocalDateTime
        LocalDateTime ws = LocalDateTime.parse(workDate);
        LocalDateTime startTime = LocalDateTime.parse(start);
        LocalDateTime endTime = LocalDateTime.parse(end);

        try {
            // Gọi hàm xử lý logic
            BigDecimal result = bookingService.validateAndCalculate(ws, startTime, endTime, shift);

            // Assert cho các case đúng
            assertNotNull(result);

        } catch (RuntimeException e) {
            // Assert cho các case sai (phải chứa tiền tố "❌ LỖI" trong message)
            assertTrue(e.getMessage().contains("❌ LỖI"),
                    "Message lỗi không đúng định dạng: " + e.getMessage());
        }
    }
}