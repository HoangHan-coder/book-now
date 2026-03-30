package vn.edu.fpt.booknow.controllers.client;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.edu.fpt.booknow.model.entities.Booking;
import vn.edu.fpt.booknow.services.BookingService;

@Controller
@RequestMapping("/staff")
public class CheckInOfflineController {
    private BookingService bookingService;
    public CheckInOfflineController(BookingService bookingService) {
        this.bookingService= bookingService;
    }
    @GetMapping("/offline-checkin")
    public String showCheckinPage(@RequestParam(value = "searchTerm", required = false) String searchTerm,
                                  Model model, RedirectAttributes redirectAttributes) {
        if (searchTerm != null && !searchTerm.isEmpty()) {
            Booking booking = bookingService.getFindCode(searchTerm);
            if (booking == null) {
                System.out.println(booking + " 24444");
                redirectAttributes.addFlashAttribute("toastMessage", "Không tìm thấy đơn!");
                redirectAttributes.addFlashAttribute("toastType", "error");
                return "redirect:/staff/offline-checkin"; // Thay đổi URL cho đúng với RequestMapping của bạn
            }
            model.addAttribute("booking", booking);
            model.addAttribute("hasResult", true);
            model.addAttribute("searchTerm", searchTerm);
        }
        return "public/staff_offline_checkin_wireframe";
    }
    @PostMapping("/complete-checkin") // Khớp với th:action trong HTML
    public String save(@ModelAttribute Booking booking,
                       @RequestParam(value = "cccdFront", required = false) MultipartFile frontImg,
                       @RequestParam(value = "cccdBack", required = false) MultipartFile backImg,
                       @CookieValue(name = "Access_token", required = false) String accessToken,
                       RedirectAttributes redirectAttributes) {
        try {

            long MAX_SIZE = 5 * 1024 * 1024;
            if ((frontImg != null && frontImg.getSize() > MAX_SIZE) ||
                    (backImg != null && backImg.getSize() > MAX_SIZE)) {
                redirectAttributes.addFlashAttribute("toastMessage", "Ảnh vượt quá 5MB!");
                redirectAttributes.addFlashAttribute("toastType", "error");
                return "redirect:/staff/offline-checkin?searchTerm=" + booking.getBookingCode();
            }

            return bookingService.completeOfflineCheckin(booking, frontImg, backImg, redirectAttributes);

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("toastMessage", "Có lỗi xảy ra: " + e.getMessage());
            redirectAttributes.addFlashAttribute("toastType", "error");
            return "redirect:/staff/offline-checkin";
        }
    }
}
