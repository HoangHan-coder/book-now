package vn.edu.fpt.booknow.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vn.edu.fpt.booknow.model.entities.Booking;
import vn.edu.fpt.booknow.repositories.BookingRepository;

import java.util.List;

@Service
public class BookingService {

    @Autowired
    private BookingRepository bookingRepo;

    public List<Booking> getBookingByEmail(String email) {
        return bookingRepo.getBookingByCustomer_Email(email).orElse(null);
    }
}
