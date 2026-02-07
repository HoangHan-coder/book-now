package vn.edu.fpt.booknow.services;

import org.springframework.stereotype.Service;
import vn.edu.fpt.booknow.entities.Booking;
import vn.edu.fpt.booknow.repositories.BookingRepository;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

@Service
public class BookingServices {
    private BookingRepository bookingRepository;

}
