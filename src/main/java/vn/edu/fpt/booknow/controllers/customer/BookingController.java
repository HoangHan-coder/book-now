package vn.edu.fpt.booknow.controllers.customer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import vn.edu.fpt.booknow.model.entities.Booking;
import vn.edu.fpt.booknow.services.BookingService;
import vn.edu.fpt.booknow.services.JWTService;

import java.util.List;

@Controller
@RequestMapping("/bookings")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private JWTService jwtService;

    @GetMapping("/history")
    public String historyBookingView(Model model,
                                     @CookieValue(value = "Access_token", required = false) String token) {

        String email = token != null ? jwtService.extractUserName(token) : null;
        if (email == null) return "redirect:/auth/login";

        List<Booking> bookings = bookingService.getBookingByEmail(email);
        model.addAttribute("bookings", bookings);
        return "private/customer/booking_history";
    }


}
