package vn.edu.fpt.booknow.controllers.client;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.edu.fpt.booknow.model.dto.BookingCustomerDTO;
import vn.edu.fpt.booknow.model.dto.BookingDTO;
import vn.edu.fpt.booknow.services.BookingService;

@Controller
@RequestMapping
public class BookingClientController {
    private BookingService bookingService;

    public BookingClientController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping("/booking/save")
    public String bookingSave(@ModelAttribute BookingCustomerDTO bookingDTO, @RequestParam(value = "cccd_front", required = false) MultipartFile frontImg,
                              @RequestParam(value = "cccd_back", required = false) MultipartFile backImg,
                              @CookieValue(name = "Access_token", required = false) String accessToken,
                              RedirectAttributes redirectAttributes, Model model) {
        try {
            long MAX_SIZE = 5 * 1024 * 1024; // 5MB
            if (frontImg != null && frontImg.getSize() > MAX_SIZE) {
                redirectAttributes.addFlashAttribute("toastMessage", "Ảnh mặt trước vượt quá 5MB!");
                redirectAttributes.addFlashAttribute("toastType", "error");
                return "redirect:/detail/" + bookingDTO.getRoom().getRoomId();
            }

            if (backImg != null && backImg.getSize() > MAX_SIZE) {
                redirectAttributes.addFlashAttribute("toastMessage", "Ảnh mặt sau vượt quá 5MB!");
                redirectAttributes.addFlashAttribute("toastType", "error");
                return "redirect:/detail/" + bookingDTO.getRoom().getRoomId();
            }
            if (accessToken == null || accessToken.isEmpty()) {
                System.out.println("sai access token........................");
                return "redirect:/auth/login";
            }
            String rediect = bookingService.saveBooking(bookingDTO, frontImg, backImg, redirectAttributes, accessToken);
            return rediect;

        } catch (Exception e) {
            System.out.println("chưa đăng nhập..........................");
            return "redirect:/auth/login";
        }
    }
}
