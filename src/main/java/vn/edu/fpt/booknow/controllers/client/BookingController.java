package vn.edu.fpt.booknow.controllers.client;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.edu.fpt.booknow.model.dto.BookingDTO;
import vn.edu.fpt.booknow.services.BookingService;
@Controller
public class BookingController {
    private BookingService bookingService;
    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping("/booking/save")
    public String bookingSave(@ModelAttribute BookingDTO bookingDTO, @RequestParam(value = "cccd_front", required = false) MultipartFile frontImg,
                              @RequestParam(value = "cccd_back", required = false) MultipartFile backImg,
//                              @CookieValue(name = "Access_token", required = false) String accessToken,
                              RedirectAttributes redirectAttributes, Model model) {


        try {
            System.out.println(frontImg.getSize());
            long MAX_SIZE = 5 * 1024 * 1024; // 5MB

            if (frontImg != null && frontImg.getSize() > MAX_SIZE) {
                redirectAttributes.addFlashAttribute("toastMessage", "Ảnh mặt trước vượt quá 5MB!");
                redirectAttributes.addFlashAttribute("toastType", "error");
                return "redirect:/detail/" + bookingDTO.getRoomId();
            }

            if (backImg != null && backImg.getSize() > MAX_SIZE) {
                redirectAttributes.addFlashAttribute("toastMessage", "Ảnh mặt sau vượt quá 5MB!");
                redirectAttributes.addFlashAttribute("toastType", "error");
                return "redirect:/detail/" + bookingDTO.getRoomId();
            }
//            if (accessToken == null || accessToken.isEmpty()) {
//                return "redirect:/auth/login";
//            }
            String rediect = bookingService.saveBooking(bookingDTO, frontImg, backImg, redirectAttributes, "", model);
            return rediect;

        } catch (Exception e) {
            System.out.println("test");
            System.out.println(e.getMessage());
            return "redirect:/auth/login";
        }
    }
}
