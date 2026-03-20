package vn.edu.fpt.booknow.controllers.customer;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.edu.fpt.booknow.model.entities.Customer;
import vn.edu.fpt.booknow.services.UpdateProfileService;

import java.security.Principal;

@Controller
@RequestMapping("/user")
public class CusUpdateProController {

    private final UpdateProfileService updateProfileService;

    public CusUpdateProController(UpdateProfileService updateProfileService) {
        this.updateProfileService = updateProfileService;
    }

    @GetMapping("/update-profile")
    public String showUpdateProfilePage(Principal principal, Model model) {
        if (principal == null) {
            return "redirect:/auth/login";
        }
        Customer customer = updateProfileService.checkCustomerExistByEmail(principal.getName());
        model.addAttribute("customer", customer);
        return "/private/customer-update-profile";
    }

    @PostMapping("/update-profile")
    public String updateProfile(
            Principal principal,
            @RequestParam("fullName") String fullName,
            @RequestParam("phoneNumber") String phoneNumber,
            @RequestParam(value = "avatar", required = false) MultipartFile avatar,
            RedirectAttributes redirectAttributes) {

        if (principal == null) {
            return "redirect:/auth/login";
        }
        try {
            updateProfileService.updateProfile(principal.getName(), fullName, phoneNumber, avatar);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật hồ sơ thành công!");
            return "redirect:/user/update-profile";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
            return "redirect:/user/update-profile";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi hệ thống: " + e.getMessage());
            return "redirect:/user/update-profile";
        }
    }
}
