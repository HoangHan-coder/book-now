package vn.edu.fpt.booknow.junit;

import org.junit.jupiter.api.Test;
import vn.edu.fpt.booknow.model.entities.Booking;
import vn.edu.fpt.booknow.services.staff.BookingDetailService;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class BookingDetailServiceTest {

    @Test
    void testCalculateDuration() {

        Booking booking = new Booking();

        booking.setCheckInTime(LocalDateTime.of(2025,1,1,10,0));
        booking.setCheckOutTime(LocalDateTime.of(2025,1,1,12,30));

        BookingDetailService service = new BookingDetailService(null);

        String result = service.calculateDuration(booking);

        assertEquals("2 giờ 30 phút", result);
    }
}