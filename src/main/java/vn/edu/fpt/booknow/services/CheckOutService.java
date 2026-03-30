package vn.edu.fpt.booknow.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.fpt.booknow.model.entities.Booking;
import vn.edu.fpt.booknow.model.entities.BookingStatus;
import vn.edu.fpt.booknow.repositories.BookingRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Service
public class CheckOutService {
    @Autowired
    private BookingRepository bookingRepository;

    @Transactional
    public String checkOut(String bookingCode) {

        Booking booking = bookingRepository.findByBookingCode(bookingCode).
                orElseThrow(() -> new RuntimeException("Booking not found"));

        if (booking.getBookingStatus() != BookingStatus.CHECKED_IN) {
            return "Booking chưa check-in";
        }

        if (booking.getTotalAmount() == null ||
                booking.getTotalAmount().compareTo(BigDecimal.ZERO) <= 0) {
            return "Chưa thanh toán";
        }

        booking.setBookingStatus(BookingStatus.CHECKED_OUT);
        booking.setUpdatedAt(LocalDateTime.now());
        booking.setActualCheckOutTime(LocalDateTime.now());
        bookingRepository.save(booking);
        return "Check-out thành công";
    }
}


