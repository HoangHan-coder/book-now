package vn.edu.fpt.booknow.controllers.staff;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import vn.edu.fpt.booknow.model.entities.Booking;
import vn.edu.fpt.booknow.services.staff.BookingDetailService;

@Controller
@RequestMapping("/admin")
public class StaBookingDetailController {

    private final BookingDetailService bookingDetailService;

    public StaBookingDetailController(BookingDetailService bookingDetailService) {
        this.bookingDetailService = bookingDetailService;
    }

    @GetMapping("/booking-detail/{bookingCode}")
    public String getBookingDetail(@PathVariable("bookingCode") String bookingCode,
                                   Model model) {

        Booking booking = bookingDetailService.getBookingDetail(bookingCode);

        // BẮT LỖI Ở CONTROLLER (không throw)
        if (booking == null) {
            model.addAttribute("errorMessage", "Không tìm thấy booking");
            return "error/404";
        }

        String durationText = bookingDetailService.calculateDuration(booking);

        model.addAttribute("booking", booking);
        model.addAttribute("durationText", durationText);

        return "private/staff-booking-detail";
    }
}