package vn.edu.fpt.booknow.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
//import vn.edu.fpt.booknow.dto.BookingUpdateMessage;
import vn.edu.fpt.booknow.model.entities.Booking;
import vn.edu.fpt.booknow.model.entities.BookingStatus;
import vn.edu.fpt.booknow.repositories.BookingRepository;

@Service
public class BookingUpdateService {

    private final BookingRepository bookingRepository;

    private final MailService emailService;

    public BookingUpdateService(BookingRepository bookingRepository,

                                MailService emailService) {
        this.bookingRepository = bookingRepository;

        this.emailService = emailService;
    }

    @Transactional
    public void updateStatus(String bookingCode, BookingStatus newStatus, String reason) {

        Booking booking = getBookingOrThrow(bookingCode);

        validateStatusTransition(booking.getBookingStatus(), newStatus);

        booking.setBookingStatus(newStatus);
        System.out.println(newStatus);
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

        if (newStatus == BookingStatus.REJECTED_CHECKIN){
            emailService.sendReasonFailed(
                    booking.getCustomer().getEmail(),
                    booking.getBookingCode(),
                    reason
            );
        }
        System.out.println(bookingRepository.save(booking));


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
            case PENDING_PAYMENT -> next == BookingStatus.PAID ||
                    next == BookingStatus.FAILED;

            case PAID ->
                    next == BookingStatus.APPROVED ||
            next == BookingStatus.REJECTED;

            case APPROVED ->
                    next == BookingStatus.CHECKED_IN ||
                            next == BookingStatus.REJECTED_CHECKIN ||
               next == BookingStatus.FAILED;

            case CHECKED_IN -> next == BookingStatus.CHECKED_OUT ||
                    next == BookingStatus.REJECTED;
            case CHECKED_OUT -> next == BookingStatus.COMPLETED;
            case REJECTED -> next == BookingStatus.APPROVED;
            case REJECTED_CHECKIN -> next == BookingStatus.CHECKED_IN || next == BookingStatus.REJECTED_CHECKIN;

            default -> false;
        };
    }

}