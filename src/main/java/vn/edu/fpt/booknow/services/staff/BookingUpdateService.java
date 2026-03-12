package vn.edu.fpt.booknow.services.staff;

import org.springframework.stereotype.Service;
import vn.edu.fpt.booknow.entities.Booking;
import vn.edu.fpt.booknow.entities.BookingStatus;
import vn.edu.fpt.booknow.repositories.BookingRepository;

@Service
public class BookingUpdateService {

    private final BookingRepository bookingRepository;

    public BookingUpdateService(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    public void updateStatus(String bookingCode, BookingStatus newStatus, String reason) {

        Booking booking = getBookingOrThrow(bookingCode);

        validateStatusTransition(booking.getBookingStatus(), newStatus);
        if (newStatus == BookingStatus.REJECT) {

            booking.setNote(reason);
            return;

            // reset CCCD để khách upload lại
        }

        booking.setBookingStatus(newStatus);



        bookingRepository.save(booking);
    }

    public Booking getBookingOrThrow(String bookingCode) {
        return bookingRepository.findByBookingCode(bookingCode)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy booking với mã: " + bookingCode));
    }

    public void validateStatusTransition(BookingStatus current, BookingStatus next) {

        if (!isValidTransition(current, next)) {
            throw new RuntimeException(
                    "Không thể chuyển trạng thái từ " + current + " sang " + next
            );
        }
    }

    private boolean isValidTransition(BookingStatus current, BookingStatus next) {

        return switch (current) {

            case PENDING ->
                    next == BookingStatus.PENDING_PAYMENT ||
                            next == BookingStatus.REJECT;

            case PENDING_PAYMENT ->
                    next == BookingStatus.PAID ||
                            next == BookingStatus.FAILED;

            case PAID ->
                    next == BookingStatus.CHECKED_IN || next == BookingStatus.REJECT;
            case CHECKED_IN ->
                    next == BookingStatus.CHECKED_OUT ||
                            next == BookingStatus.REJECT;
            case CHECKED_OUT -> next == BookingStatus.COMPLETED;


            default -> false;
        };
    }
}