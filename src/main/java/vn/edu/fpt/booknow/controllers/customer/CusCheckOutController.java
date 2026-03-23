package vn.edu.fpt.booknow.controllers.customer;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
//import vn.edu.fpt.booknow.services.customer.BookingService;
import vn.edu.fpt.booknow.model.entities.Booking;
import vn.edu.fpt.booknow.services.BookingService;
import vn.edu.fpt.booknow.services.CheckOutService;

import java.security.Principal;

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
    public String showCheckOutPage(@PathVariable("bookingCode") String bookingCode,
                                   Principal principal, Model model,
                                   RedirectAttributes redirectAttributes) {
        if (principal == null) {
            return "redirect:/auth/login";
        }
        Booking booking = bookingService.getBookingDetail(bookingCode);
        if (booking == null) {
            model.addAttribute("errorMessage", "Không tìm thấy booking");
            return "error/404";
        }
        // Kiểm tra booking thuộc về user đang đăng nhập
        if (!booking.getCustomer().getEmail().equals(principal.getName())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bạn không có quyền thực hiện hành động này");
            return "redirect:/";
        }
        model.addAttribute("booking", booking);
        return "private/customer-checked-out";
    }

    @PostMapping("/check-out/{bookingCode}/checkout")
    public String checkout(@PathVariable("bookingCode") String bookingCode,
                           Principal principal,
                           RedirectAttributes redirectAttributes) {
        if (principal == null) {
            return "redirect:/auth/login";
        }
        // Kiểm tra booking thuộc về user đang đăng nhập
        Booking booking = bookingService.getBookingDetail(bookingCode);


        if (booking == null) {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy booking");
            return "error/404";
        }
        if (!booking.getCustomer().getEmail().equals(principal.getName())) {
            redirectAttributes.addFlashAttribute("error", "Bạn không có quyền thực hiện hành động này");
            return "redirect:/bookings/"+ booking.getBookingId();
        }
        String message = checkOutService.checkOut(bookingCode);
        if (message.equals("Check-out thành công")) {
            redirectAttributes.addFlashAttribute("success", message);
        } else {
            redirectAttributes.addFlashAttribute("error", message);
        }
        return "redirect:/bookings/" + booking.getBookingId();
    }
}
