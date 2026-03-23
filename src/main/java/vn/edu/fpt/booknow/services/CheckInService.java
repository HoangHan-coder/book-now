package vn.edu.fpt.booknow.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vn.edu.fpt.booknow.model.entities.Booking;
import vn.edu.fpt.booknow.repositories.BookingRepository;

@Service
public class CheckInService {
    @Autowired
    BookingRepository bookingRepository;

    public CheckInService(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }
    public CheckInService() {
    }

    public Booking findById(Long bookingId) {
        return bookingRepository.findById(bookingId).orElse(null);
    }

    public Booking save(Booking booking) {
        return bookingRepository.save(booking);
    }
}
