package vn.edu.fpt.booknow.services;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.junit.jupiter.api.Assertions.*;
@ExtendWith(MockitoExtension.class)
class BookingServiceTest {
    @InjectMocks
    private BookingService bookingService;

    @ParameterizedTest(name = "Phòng={0}, Ca={1}, Slot={2}, VIP={3} => Kỳ vọng={4}")
    @CsvSource({
            // --- 1. KIỂM THỬ LOẠI PHÒNG (RoomType) ---
            "'Ocean City', 'Sáng', 1, false, 95000",   // Base 100k
            "'Mellow',     'Sáng', 1, false, 151050",  // Base 159k (159k * 0.95)
            "'Unknown',    'Sáng', 1, false, -1",      // RoomType không hợp lệ -> -1

            // --- 2. KIỂM THỬ CÁC CA (ShiftType) ---
            "'Ocean City', 'Sáng',  1, false, 95000",   // x 1.0
            "'Ocean City', 'Chiều', 1, false, 104500",  // x 1.1
            "'Ocean City', 'Tối',   1, false, 114000",  // x 1.2
            "'Ocean City', 'Đêm',   1, false, 142500",  // x 1.5
            "'Ocean City', 'Khác',  1, false, -1",      // ShiftType không hợp lệ -> -1

            // --- 3. KIỂM THỬ BIÊN SLOT (SlotCount) ---
            "'Ocean City', 'Sáng',  3, false, 285000",  // Biên 3: Giảm 5% (300k * 0.95)
            "'Ocean City', 'Sáng',  4, false, 372000",  // Biên 4: Giảm 7% (400k * 0.93)

            // --- 4. KIỂM THỬ VIP (isVip) ---
            "'Ocean City', 'Sáng',  1, true,  85000",   // 5% (slot) + 10% (VIP) = 15% -> 100k * 0.85

    })
    void testCalculatePricing(String roomType, String shiftType, int slotCount, boolean isVip, long expectedAmount) {
        // Gọi hàm thực tế trả về long
        long result = bookingService.calculatePricing(roomType, shiftType, slotCount, isVip);
        System.out.println("Result: " + result);
        // So sánh trực tiếp hai giá trị long
        assertEquals(expectedAmount, result,
                String.format("Sai logic tại: %s, %s, %d slots, VIP: %b", roomType, shiftType, slotCount, isVip));
    }
}