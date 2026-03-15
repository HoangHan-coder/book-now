package vn.edu.fpt.booknow.services;

import org.springframework.stereotype.Service;
import vn.edu.fpt.booknow.model.entities.Booking;
import vn.edu.fpt.booknow.model.entities.BookingStatus;
import vn.edu.fpt.booknow.repositories.BookingRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class BookingListService {

    private final BookingRepository bookingRepository;

    public BookingListService(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    public List<Booking> getAllBooking() {
        return bookingRepository.findAllWithCustomer();
    }
    public List<Booking> filter(
            String fromDate,
            String toDate,
            BookingStatus status,
            String keyword
    ) {

        final LocalDateTime from =
                (fromDate != null && !fromDate.isBlank())
                        ? LocalDate.parse(fromDate).atStartOfDay()
                        : null;

        final LocalDateTime to =
                (toDate != null && !toDate.isBlank())
                        ? LocalDate.parse(toDate).atTime(23, 59, 59)
                        : null;

        final String search =
                (keyword != null && !keyword.isBlank())
                        ? keyword.trim().toLowerCase()
                        : null;

        return bookingRepository.findAllWithCustomer().stream()
                .filter(b -> {

                    // 1️⃣ Check-in
                    if (from != null && b.getCheckInTime().isBefore(from)) {
                        return false;
                    }

                    // 2️⃣ Check-out
                    if (to != null && b.getCheckOutTime().isAfter(to)) {
                        return false;
                    }

                    // 3️⃣ Status
                    if (status != null && b.getBookingStatus() != status) {
                        return false;
                    }

                    // 4️⃣ Keyword
                    if (search != null) {

                        boolean matchCode =
                                b.getBookingCode() != null &&
                                        b.getBookingCode().toLowerCase().contains(search);

                        boolean matchName =
                                b.getCustomer() != null &&
                                        b.getCustomer().getFullName() != null &&
                                        b.getCustomer().getFullName().toLowerCase().contains(search);

                        if (!matchCode && !matchName) {
                            return false;
                        }
                    }

                    return true;
                })
                .toList();
    }
}