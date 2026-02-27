package vn.edu.fpt.booknow.controller.customer;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.edu.fpt.booknow.services.customer.ChangePasswordService;

import java.util.Map;

@Controller
@RequestMapping("/user")
public class CusChangePassController {

    private final ChangePasswordService changePasswordService;

    public CusChangePassController(ChangePasswordService changePasswordService) {
        this.changePasswordService = changePasswordService;
    }

    // ===== GET =====
    @GetMapping("/customer-change-password/{id}")
    public String showChangePasswordPage(
            @PathVariable("id") Long customerId,
            Model model
    ) {
        if (customerId == null || customerId <= 0) {
            return "error/400";
        }

        // customerId có thể đã có từ flash, nhưng add lại cũng không sao
        model.addAttribute("customerId", customerId);
        return "private/customer-change-password";
    }

    // ===== POST =====
    @PostMapping("/change-password/{id}")
    public String changePassword(
            @PathVariable("id") Long customerId,
            @RequestParam("currentPassword") String currentPassword,
            @RequestParam("newPassword") String newPassword,
            @RequestParam("confirmPassword") String confirmPassword,
            RedirectAttributes redirectAttributes
    ) {
        try {
            Map<String, String> errors = changePasswordService.changePassword(
                    customerId,
                    currentPassword,
                    newPassword,
                    confirmPassword
            );

            if (!errors.isEmpty()) {
                redirectAttributes.addFlashAttribute("errors", errors);
                redirectAttributes.addFlashAttribute("customerId", customerId);
                return "redirect:/user/customer-change-password/" + customerId;
            }

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Đổi mật khẩu thành công"
            );
            redirectAttributes.addFlashAttribute("customerId", customerId);

            return "redirect:/user/customer-change-password/" + customerId;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errors", 
                    java.util.Collections.singletonMap("global", "Lỗi hệ thống: " + e.getMessage()));
            redirectAttributes.addFlashAttribute("customerId", customerId);
            return "redirect:/user/customer-change-password/" + customerId;
        }
    }
}
