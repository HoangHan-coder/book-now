package vn.edu.fpt.booknow.services.staff;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.edu.fpt.booknow.model.entities.Booking;
import vn.edu.fpt.booknow.model.entities.BookingStatus;
import vn.edu.fpt.booknow.model.entities.Customer;
import vn.edu.fpt.booknow.repositories.BookingRepository;
import vn.edu.fpt.booknow.services.BookingListService;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests cho BookingListService – luồng tìm kiếm booking của Staff.
 *
 * Coverage:
 *  1. Tìm theo mã booking (bookingCode)
 *  2. Tìm theo tên khách hàng (fullName)
 *  3. Không có kết quả khớp
 *  4. Keyword null → trả về tất cả
 *  5. Keyword rỗng/khoảng trắng → trả về tất cả
 *  6. Tìm kiếm không phân biệt hoa thường
 *  7. getAllBooking() gọi đúng repository method
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("BookingListService – Staff Search Booking")
class BookingSearchServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @InjectMocks
    private BookingListService bookingListService;

    /** Data dùng chung cho các test case */
    private List<Booking> sampleBookings;

    @BeforeEach
    void setUp() {
        // ── Khách hàng ─────────────────────────────────────────────────────────
        Customer customerA = new Customer();
        customerA.setFullName("Nguyen Van A");

        Customer customerB = new Customer();
        customerB.setFullName("Tran Thi B");

        // ── Booking 1 ───────────────────────────────────────────────────────────
        Booking b1 = new Booking();
        b1.setBookingCode("BK001");
        b1.setCustomer(customerA);
        b1.setBookingStatus(BookingStatus.PENDING);
        b1.setCheckInTime(LocalDateTime.of(2026, 3, 1, 14, 0));
        b1.setCheckOutTime(LocalDateTime.of(2026, 3, 5, 12, 0));

        // ── Booking 2 ───────────────────────────────────────────────────────────
        Booking b2 = new Booking();
        b2.setBookingCode("BK002");
        b2.setCustomer(customerB);
        b2.setBookingStatus(BookingStatus.PAID);
        b2.setCheckInTime(LocalDateTime.of(2026, 3, 10, 14, 0));
        b2.setCheckOutTime(LocalDateTime.of(2026, 3, 15, 12, 0));

        sampleBookings = List.of(b1, b2);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // 1. Tìm theo mã booking
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("TC01 – Tìm theo mã booking 'BK001' → trả về đúng 1 booking")
    void searchByBookingCode_shouldReturnMatchingBooking() {
        when(bookingRepository.findAllWithCustomer()).thenReturn(sampleBookings);

        List<Booking> result = bookingListService.filter(null, null, null, "BK001");

        assertEquals(1, result.size(), "Phải trả về đúng 1 kết quả");
        assertEquals("BK001", result.get(0).getBookingCode());
        verify(bookingRepository, times(1)).findAllWithCustomer();
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // 2. Tìm theo tên khách hàng
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("TC02 – Tìm theo tên khách 'Nguyen Van A' → trả về đúng booking của A")
    void searchByCustomerName_shouldReturnMatchingBooking() {
        when(bookingRepository.findAllWithCustomer()).thenReturn(sampleBookings);

        List<Booking> result = bookingListService.filter(null, null, null, "Nguyen Van A");

        assertEquals(1, result.size(), "Phải trả về đúng 1 kết quả cho khách Nguyen Van A");
        assertEquals("Nguyen Van A", result.get(0).getCustomer().getFullName());
        verify(bookingRepository, times(1)).findAllWithCustomer();
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // 3. Không tìm được kết quả nào
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("TC03 – Keyword không khớp bất kỳ booking nào → danh sách rỗng")
    void searchWithNoMatch_shouldReturnEmptyList() {
        when(bookingRepository.findAllWithCustomer()).thenReturn(sampleBookings);

        List<Booking> result = bookingListService.filter(null, null, null, "BOOKING_KHONG_TON_TAI");

        assertTrue(result.isEmpty(), "Kết quả phải rỗng khi không có booking khớp");
        verify(bookingRepository, times(1)).findAllWithCustomer();
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // 4. Keyword null → không lọc → trả về tất cả
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("TC04 – Keyword null → trả về tất cả bookings (không lọc)")
    void searchWithNullKeyword_shouldReturnAll() {
        when(bookingRepository.findAllWithCustomer()).thenReturn(sampleBookings);

        List<Booking> result = bookingListService.filter(null, null, null, null);

        assertEquals(2, result.size(), "Phải trả về toàn bộ 2 bookings khi keyword null");
        verify(bookingRepository, times(1)).findAllWithCustomer();
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // 5. Keyword rỗng / chỉ toàn khoảng trắng → trả về tất cả
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("TC05 – Keyword là chuỗi rỗng → trả về tất cả bookings")
    void searchWithBlankKeyword_shouldReturnAll() {
        when(bookingRepository.findAllWithCustomer()).thenReturn(sampleBookings);

        List<Booking> result = bookingListService.filter(null, null, null, "   ");

        assertEquals(2, result.size(), "Phải trả về toàn bộ 2 bookings khi keyword chỉ toàn khoảng trắng");
        verify(bookingRepository, times(1)).findAllWithCustomer();
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // 6. Tìm kiếm không phân biệt hoa thường
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("TC06 – Tìm 'nguyen van a' (chữ thường) → vẫn trả về kết quả")
    void searchCaseInsensitive_shouldReturnResult() {
        when(bookingRepository.findAllWithCustomer()).thenReturn(sampleBookings);

        List<Booking> result = bookingListService.filter(null, null, null, "nguyen van a");

        assertEquals(1, result.size(), "Tìm kiếm cần không phân biệt hoa thường");
        verify(bookingRepository, times(1)).findAllWithCustomer();
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // 7. getAllBooking() – lấy toàn bộ danh sách
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("TC07 – getAllBooking() gọi đúng repository và trả về toàn bộ danh sách")
    void getAllBooking_shouldDelegateToRepository() {
        when(bookingRepository.findAllWithCustomer()).thenReturn(sampleBookings);

        List<Booking> result = bookingListService.getAllBooking();

        assertEquals(2, result.size());
        verify(bookingRepository, times(1)).findAllWithCustomer();
        verifyNoMoreInteractions(bookingRepository);
    }
}
