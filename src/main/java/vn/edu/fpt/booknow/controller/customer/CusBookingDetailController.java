package vn.edu.fpt.booknow.controller.customer;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import vn.edu.fpt.booknow.entities.Booking;
import vn.edu.fpt.booknow.repositories.BookingRepository;

@Controller
@RequestMapping("/user")
public class CusBookingDetailController {

    private final BookingRepository bookingRepository;

    public CusBookingDetailController(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    @GetMapping("/booking-detail/{bookingCode}")
    public String getBookingDetail(
            @PathVariable("bookingCode") String bookingCode,
            Model model) {

        Booking booking = bookingRepository.findByBookingCode(bookingCode)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        model.addAttribute("booking", booking);

        return "/private/customer-booking-detail";
        // -> src/main/resources/templates/customer/booking-detail.html
    }
}