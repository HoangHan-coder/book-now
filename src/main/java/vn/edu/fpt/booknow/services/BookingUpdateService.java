package vn.edu.fpt.booknow.services;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.fpt.booknow.model.entities.Booking;
import vn.edu.fpt.booknow.model.entities.BookingStatus;
import vn.edu.fpt.booknow.repositories.BookingRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class BookingUpdateService {

    private final BookingRepository bookingRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final MailService emailService;
    public BookingUpdateService(BookingRepository bookingRepository,
                                SimpMessagingTemplate messagingTemplate,
                                MailService emailService) {
        this.bookingRepository = bookingRepository;
        this.messagingTemplate = messagingTemplate;
        this.emailService = emailService;
    }

    @Transactional
    public void updateStatus(String bookingCode, BookingStatus newStatus, String reason) {

        Booking booking = getBookingOrThrow(bookingCode);

        validateStatusTransition(booking.getBookingStatus(), newStatus);

        booking.setBookingStatus(newStatus);

        if (newStatus == BookingStatus.REJECTED) {

            booking.setNote(reason);

            emailService.sendReasonReject(
                    booking.getCustomer().getEmail(),
                    booking.getBookingCode(),
                    reason
            );
        }

        if (newStatus == BookingStatus.FAILED) {

            emailService.sendReasonFailed(
                    booking.getCustomer().getEmail(),
                    booking.getBookingCode(),
                    reason
            );
        }

        Booking savedBooking = bookingRepository.save(booking);


    }


    public Booking getBookingOrThrow(String bookingCode) {
        return bookingRepository.findByBookingCodeWithDetails(bookingCode)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy booking với mã: " + bookingCode));
    }

    private void validateStatusTransition(BookingStatus current, BookingStatus next) {

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
                    next == BookingStatus.REJECTED ||
                            next == BookingStatus.FAILED;

            case PENDING_PAYMENT ->
                    next == BookingStatus.PAID ||
                            next == BookingStatus.FAILED;

            case PAID ->
                    next == BookingStatus.CHECKED_IN;
            case CHECKED_IN ->
                    next == BookingStatus.CHECKED_OUT ||
                    next == BookingStatus.REJECTED;
            case CHECKED_OUT -> next == BookingStatus.COMPLETED;


            default -> false;
        };
    }
    @Transactional
    public void autoCheckOutExpiredBookings() {

        List<Booking> checkedInBookings = bookingRepository.findByBookingStatus(BookingStatus.CHECKED_IN);

        LocalDateTime now = LocalDateTime.now();

        for (Booking booking : checkedInBookings) {

            if (booking.getCheckOutTime() != null &&
                    booking.getCheckOutTime().isBefore(now)) {

                booking.setBookingStatus(BookingStatus.CHECKED_OUT);

                Booking savedBooking = bookingRepository.save(booking);
            }
        }
    }
}