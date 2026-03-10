package vn.edu.fpt.booknow.services.staff;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.fpt.booknow.dto.BookingUpdateMessage;
import vn.edu.fpt.booknow.model.entities.Booking;
import vn.edu.fpt.booknow.model.entities.BookingStatus;
import vn.edu.fpt.booknow.repositories.BookingRepository;

@Service
public class BookingUpdateService {

    private final BookingRepository bookingRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public BookingUpdateService(BookingRepository bookingRepository,
                               SimpMessagingTemplate messagingTemplate) {
        this.bookingRepository = bookingRepository;
        this.messagingTemplate = messagingTemplate;
    }

    @Transactional
    public void updateStatus(String bookingCode, BookingStatus newStatus, String reason) {

        Booking booking = getBookingOrThrow(bookingCode);

        validateStatusTransition(booking.getBookingStatus(), newStatus);

        booking.setBookingStatus(newStatus);

        if (newStatus == BookingStatus.REJECT) {

            booking.setNote(reason);

            // reset CCCD để khách upload lại
        }

        Booking savedBooking = bookingRepository.save(booking);

        // Send real-time update via WebSocket
        notifyBookingUpdate(savedBooking);
    }

    private void notifyBookingUpdate(Booking booking) {
        BookingUpdateMessage message = new BookingUpdateMessage(
                booking.getBookingCode(),
                booking.getBookingStatus(),
                booking.getCustomer().getFullName(),
                booking.getRoom().getRoomNumber(),
                booking.getCheckInTime(),
                booking.getCheckOutTime(),
                booking.getUpdateAt(),
                "STATUS_CHANGED"
        );

        // Send to all staff members listening on /topic/booking-updates
        messagingTemplate.convertAndSend("/topic/booking-updates", message);
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
                            next == BookingStatus.REJECT;

            case PENDING_PAYMENT ->
                    next == BookingStatus.PAID ||
                            next == BookingStatus.FAILED;

            case PAID ->
                    next == BookingStatus.CHECKED_IN;
            case CHECKED_IN ->
                    next == BookingStatus.CHECKED_OUT ||
                    next == BookingStatus.REJECT;
            case CHECKED_OUT -> next == BookingStatus.COMPLETED;


            default -> false;
        };
    }
}