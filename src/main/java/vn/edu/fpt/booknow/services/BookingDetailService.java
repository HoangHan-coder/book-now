package vn.edu.fpt.booknow.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.fpt.booknow.model.entities.Booking;
import vn.edu.fpt.booknow.model.entities.BookingStatus;
import vn.edu.fpt.booknow.repositories.BookingRepository;

import java.time.Duration;

@Service
public class BookingDetailService {

    private final BookingRepository bookingRepository;

    public BookingDetailService(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    @Transactional(readOnly = true)
    public Booking getBookingDetail(String code) {
        return bookingRepository.findByBookingCodeWithDetails(code).orElse(null);
    }

    // ✅ Tính thời gian lưu trú đẹp
    public String calculateDuration(Booking booking) {

        if (booking.getCheckInTime() == null || booking.getCheckOutTime() == null) {
            return "Chưa xác định";
        }

        Duration duration = Duration.between(
                booking.getCheckInTime(),
                booking.getCheckOutTime()
        );

        // nếu dữ liệu lỗi
        if (duration.isNegative()) {
            return "0 giờ";
        }

        long hours = duration.toHours();
        long minutes = duration.toMinutes() % 60;

        if (hours == 0 && minutes == 0) {
            return "Dưới 1 phút";
        }

        if (minutes == 0) {
            return hours + " giờ";
        }

        return hours + " giờ " + minutes + " phút";
    }
}