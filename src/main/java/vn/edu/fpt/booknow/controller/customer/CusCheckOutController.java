package vn.edu.fpt.booknow.controller.customer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.edu.fpt.booknow.repositories.BookingRepository;
import vn.edu.fpt.booknow.services.customer.CheckOutService;

import java.security.Principal;

@Controller
@RequestMapping("/user")
public class CusCheckOutController {

    private final CheckOutService checkOutService;

    public CusCheckOutController(CheckOutService checkOutService) {
        this.checkOutService = checkOutService;

    }

    @PostMapping("/check-out/{bookingCode}/checkout")
    public String checkout(@PathVariable("bookingCode") String bookingCode,
                           RedirectAttributes redirectAttributes) {
        String message = checkOutService.checkOut(bookingCode);
        if (message.equals("Check-out thành công")) {
            redirectAttributes.addFlashAttribute("success", message);
        } else {
            redirectAttributes.addFlashAttribute("error", message);
        }

        return "redirect:/user/booking-detail/" + bookingCode;
    }
}
