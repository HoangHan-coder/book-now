package vn.edu.fpt.booknow.controllers.staff;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.edu.fpt.booknow.model.entities.Booking;
import vn.edu.fpt.booknow.model.entities.BookingStatus;
import vn.edu.fpt.booknow.model.entities.CheckInSession;
import vn.edu.fpt.booknow.services.BookingUpdateService;
import vn.edu.fpt.booknow.services.CheckInSessionService;


@Controller
@RequestMapping("/staff/bookings")
public class StaBookingUpdateStatusController {

    private final BookingUpdateService bookingUpdateService;
    private final CheckInSessionService checkInSessionService;

    public StaBookingUpdateStatusController(BookingUpdateService bookingUpdateService, CheckInSessionService checkInSessionService) {
        this.bookingUpdateService = bookingUpdateService;
        this.checkInSessionService = checkInSessionService;
    }

    @GetMapping("/update/{bookingCode}")
    public String showUpdatePage(
            @PathVariable("bookingCode") String bookingCode,
            Model model
    ) {
        try {
            Booking booking = bookingUpdateService.getBookingOrThrow(bookingCode);
            System.out.println("booking code: " + booking.getBookingCode());
            try {
                CheckInSession checkInSession = checkInSessionService.getCheckInSessionId(booking.getBookingId());
                model.addAttribute("checkInSession", checkInSession);
            } catch (Exception e) {
                model.addAttribute("massageError", "CheckInSession not found");
            }
            model.addAttribute("booking", booking);
            return "admin/staff-booking-updated-status";
        } catch (Exception e) {
            System.out.println("loi o day");
            model.addAttribute("errorMessage", e.getMessage());
            return "error/404";
        }
    }

    @PostMapping("/update-status")
    public String updateStatus(
            @RequestParam("bookingCode") String bookingCode,
            @RequestParam("status") BookingStatus status,
            @RequestParam(value = "reason", required = false) String reason,
            RedirectAttributes redirectAttributes
    ) {
        try {

            bookingUpdateService.updateStatus(bookingCode, status, reason);

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Cập nhật trạng thái thành công"
            );

        } catch (Exception e) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    e.getMessage()
            );
        }

        return "redirect:/staff/bookings/update/" + bookingCode;
    }

    @PostMapping("/cancel/{bookingCode}")
    public String cancelBooking(
            @PathVariable String bookingCode,
            @RequestParam BookingStatus status,
            @RequestParam(required = false) String reason,
            RedirectAttributes redirectAttributes
    ) {
        try {

            bookingUpdateService.updateStatus(
                    bookingCode,
                    status,
                    reason
            );

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Cập nhật trạng thái thành công"
            );

        } catch (Exception e) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    e.getMessage()
            );
        }

        return "redirect:/staff/bookings";
    }
}