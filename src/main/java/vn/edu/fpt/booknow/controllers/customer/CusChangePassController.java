package vn.edu.fpt.booknow.controllers.customer;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.edu.fpt.booknow.services.ChangePasswordService;

import java.security.Principal;
import java.util.Map;

@Controller
@RequestMapping("/user")
public class CusChangePassController {

    private final ChangePasswordService changePasswordService;

    public CusChangePassController(ChangePasswordService changePasswordService) {
        this.changePasswordService = changePasswordService;
    }

    // ===== GET =====
    @GetMapping("/customer-change-password")
    public String showChangePasswordPage(Principal principal, Model model) {
        if (principal == null) {
            return "redirect:/auth/login";
        }
        return "private/customer-change-password";
    }

    // ===== POST =====
    @PostMapping("/change-password")
    public String changePassword(
            Principal principal,
            @RequestParam("currentPassword") String currentPassword,
            @RequestParam("newPassword") String newPassword,
            @RequestParam("confirmPassword") String confirmPassword,
            RedirectAttributes redirectAttributes
    ) {
        if (principal == null) {
            return "redirect:/auth/login";
        }
        try {
            Map<String, String> errors = changePasswordService.changePassword(
                    principal.getName(),
                    currentPassword,
                    newPassword,
                    confirmPassword
            );

            if (!errors.isEmpty()) {
                redirectAttributes.addFlashAttribute("errors", errors);
                return "redirect:/user/customer-change-password";
            }

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Đổi mật khẩu thành công"
            );

            return "redirect:/user/customer-change-password";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errors",
                    java.util.Collections.singletonMap("global", "Lỗi hệ thống: " + e.getMessage()));
            return "redirect:/user/customer-change-password";
        }
    }
}
