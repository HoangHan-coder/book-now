package vn.edu.fpt.booknow.services.staff;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.edu.fpt.booknow.model.entities.Booking;
import vn.edu.fpt.booknow.repositories.BookingRepository;
import vn.edu.fpt.booknow.services.BookingDetailService;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests cho BookingDetailService – luồng xem chi tiết booking của Staff.
 *
 * Coverage:
 *  1. getBookingDetail() với mã hợp lệ → trả về booking
 *  2. getBookingDetail() với mã không tồn tại → trả về null
 *  3. calculateDuration() – có cả giờ và phút
 *  4. calculateDuration() – chỉ có giờ, phút = 0
 *  5. calculateDuration() – checkIn là null → "Chưa xác định"
 *  6. calculateDuration() – checkOut là null → "Chưa xác định"
 *  7. calculateDuration() – duration âm (checkOut trước checkIn) → "0 giờ"
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("BookingDetailService – Staff Booking Detail")
class BookingDetailServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @InjectMocks
    private BookingDetailService bookingDetailService;

    // ═══════════════════════════════════════════════════════════════════════════
    // getBookingDetail()
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("TC01 – Mã booking hợp lệ 'BK001' → trả về booking đúng")
    void getBookingDetail_validCode_shouldReturnBooking() {
        Booking booking = new Booking();
        booking.setBookingCode("BK001");
        when(bookingRepository.findByBookingCodeWithDetails("BK001"))
                .thenReturn(Optional.of(booking));

        Booking result = bookingDetailService.getBookingDetail("BK001");

        assertNotNull(result, "Kết quả không được null với mã booking tồn tại");
        assertEquals("BK001", result.getBookingCode());
        verify(bookingRepository, times(1)).findByBookingCodeWithDetails("BK001");
    }

    @Test
    @DisplayName("TC02 – Mã booking không tồn tại → trả về null")
    void getBookingDetail_unknownCode_shouldReturnNull() {
        when(bookingRepository.findByBookingCodeWithDetails("NOTFOUND"))
                .thenReturn(Optional.empty());

        Booking result = bookingDetailService.getBookingDetail("NOTFOUND");

        assertNull(result, "Kết quả phải là null khi booking không tồn tại");
        verify(bookingRepository, times(1)).findByBookingCodeWithDetails("NOTFOUND");
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // calculateDuration()
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("TC03 – Thời gian lưu trú 26 giờ 30 phút → '26 giờ 30 phút'")
    void calculateDuration_withHoursAndMinutes_shouldReturnCorrectString() {
        Booking booking = new Booking();
        booking.setCheckInTime(LocalDateTime.of(2026, 3, 1, 14, 0));
        booking.setCheckOutTime(LocalDateTime.of(2026, 3, 2, 16, 30));

        String result = bookingDetailService.calculateDuration(booking);

        assertEquals("26 giờ 30 phút", result);
    }

    @Test
    @DisplayName("TC04 – Thời gian lưu trú đúng 48 giờ (phút = 0) → '48 giờ'")
    void calculateDuration_exactHours_shouldOmitMinutes() {
        Booking booking = new Booking();
        booking.setCheckInTime(LocalDateTime.of(2026, 3, 1, 14, 0));
        booking.setCheckOutTime(LocalDateTime.of(2026, 3, 3, 14, 0));

        String result = bookingDetailService.calculateDuration(booking);

        assertEquals("48 giờ", result);
    }

    @Test
    @DisplayName("TC05 – checkIn là null → 'Chưa xác định'")
    void calculateDuration_nullCheckIn_shouldReturnUndetermined() {
        Booking booking = new Booking();
        booking.setCheckInTime(null);
        booking.setCheckOutTime(LocalDateTime.of(2026, 3, 3, 14, 0));

        String result = bookingDetailService.calculateDuration(booking);

        assertEquals("Chưa xác định", result);
    }

    @Test
    @DisplayName("TC06 – checkOut là null → 'Chưa xác định'")
    void calculateDuration_nullCheckOut_shouldReturnUndetermined() {
        Booking booking = new Booking();
        booking.setCheckInTime(LocalDateTime.of(2026, 3, 1, 14, 0));
        booking.setCheckOutTime(null);

        String result = bookingDetailService.calculateDuration(booking);

        assertEquals("Chưa xác định", result);
    }

    @Test
    @DisplayName("TC07 – checkOut trước checkIn (duration âm) → '0 giờ'")
    void calculateDuration_negativeRange_shouldReturnZero() {
        Booking booking = new Booking();
        booking.setCheckInTime(LocalDateTime.of(2026, 3, 5, 14, 0));
        booking.setCheckOutTime(LocalDateTime.of(2026, 3, 1, 14, 0));  // checkOut < checkIn

        String result = bookingDetailService.calculateDuration(booking);

        assertEquals("0 giờ", result);
    }
}
