package vn.edu.fpt.booknow.controller.customer;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.edu.fpt.booknow.entities.Customer;
import vn.edu.fpt.booknow.services.customer.ProfileService;

@Controller
@RequestMapping("/user")
public class CusProController {

    private ProfileService profileService;

    public CusProController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping("/profile/{id}")
    public String profile(Model model, @PathVariable(name ="id") long id, RedirectAttributes redirectAttributes) {
        try {
            Customer customer = profileService.profileDetailById(id);
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
