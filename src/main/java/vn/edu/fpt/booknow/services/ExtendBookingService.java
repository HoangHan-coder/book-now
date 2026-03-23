package vn.edu.fpt.booknow.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vn.edu.fpt.booknow.model.dto.NextTimeTableSlotBookingDTO;
import vn.edu.fpt.booknow.model.entities.Booking;
import vn.edu.fpt.booknow.model.entities.Timetable;
import vn.edu.fpt.booknow.repositories.BookingRepository;
import vn.edu.fpt.booknow.repositories.TimeTableRepository;

import java.time.LocalDateTime;
import java.time.LocalTime;


@Service
public class ExtendBookingService {
    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private TimeTableRepository timeTableRepository;

    public void updateCheckOutTime(Long timeId, Long bookingId) throws Exception {
        Booking booking = bookingRepository.findById(bookingId).orElse(null);
        Timetable timetable = timeTableRepository.findById(timeId).orElse(null);

        if(booking != null){
            LocalDateTime dateTime = booking.getCheckOutTime();
            LocalTime localTime = timetable.getEndTime();

            LocalDateTime result = dateTime.with(localTime);
            booking.setCheckOutTime(result);
            bookingRepository.save(booking);
        } else {
            throw new RuntimeException("booking not found");
        }


    }


}
