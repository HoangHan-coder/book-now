package vn.edu.fpt.booknow.controllers.customer;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.edu.fpt.booknow.entities.Booking;
import vn.edu.fpt.booknow.services.customer.BookingService;
import vn.edu.fpt.booknow.services.customer.CheckOutService;

@Controller
@RequestMapping("/user")
public class CusCheckOutController {

    private final CheckOutService checkOutService;
    private final BookingService bookingService;

    public CusCheckOutController(CheckOutService checkOutService, BookingService bookingService) {
        this.checkOutService = checkOutService;
        this.bookingService = bookingService;
    }

    @GetMapping("/check-out/{bookingCode}")
    public String showCheckOutPage(@PathVariable("bookingCode") String bookingCode, Model model) {
      Booking booking = bookingService.getBookingDetail(bookingCode);
      if(bookingService.getBookingDetail(bookingCode) == null
      ){
          model.addAttribute("errorMessage", "Không tìm thấy booking");
          return "error/404";
      }
      else {
          model.addAttribute("booking", booking);
          return "private/customer-checked-out";
      }
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
