package vn.edu.fpt.booknow.services.customer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vn.edu.fpt.booknow.model.entities.Booking;
import vn.edu.fpt.booknow.repositories.BookingRepository;

@Service
public class BookingService {
    @Autowired
    private BookingRepository bookingRepository;

    public Booking getBookingDetail(String code) {
        return bookingRepository.findByBookingCode(code).orElse(null);
    }
    public Booking findById(long id) {
        return bookingRepository.findById(id).orElse(null);
    }

}
