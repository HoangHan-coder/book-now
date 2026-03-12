package vn.edu.fpt.booknow.services.customer;

import org.springframework.stereotype.Service;
import vn.edu.fpt.booknow.model.entities.Booking;
import vn.edu.fpt.booknow.repositories.BookingRepository;

@Service
public class BookingService {

   private final BookingRepository bookingRepository;

   public BookingService(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    public Booking getBookingDetail(String code) {
       return bookingRepository.findByBookingCode(code).orElse(null);
    }
}
