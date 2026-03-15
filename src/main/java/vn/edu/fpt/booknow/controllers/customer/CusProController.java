package vn.edu.fpt.booknow.controllers.customer;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.edu.fpt.booknow.model.entities.Customer;
import vn.edu.fpt.booknow.services.ProfileService;

import java.security.Principal;

@Controller
@RequestMapping("/user")
public class CusProController {

    private ProfileService profileService;

    public CusProController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping("/profile")
    public String profile(Model model, Principal principal, RedirectAttributes redirectAttributes) {
        if (principal == null) {
            return "redirect:/auth/login";
        }
        try {
            Customer customer = profileService.profileDetailByEmail(principal.getName());
            model.addAttribute("customerProfile", customer);
            return "/private/customer-profile";
        } catch (EntityNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Khách hàng không tồn tại");
            return "redirect:/";
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi hệ thống: " + e.getMessage());
            return "redirect:/";
        }
    }
}
