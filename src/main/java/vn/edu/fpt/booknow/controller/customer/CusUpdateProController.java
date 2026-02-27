package vn.edu.fpt.booknow.controller.customer;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.edu.fpt.booknow.entities.Customer;
import vn.edu.fpt.booknow.repositories.CustomerRepository;
import vn.edu.fpt.booknow.services.customer.UpdateProfileService;

@Controller
@RequestMapping("/user")
public class CusUpdateProController {

    private final UpdateProfileService updateProfileService;
    private final CustomerRepository customerRepository;

    public CusUpdateProController(UpdateProfileService updateProfileService,
                                  CustomerRepository customerRepository) {
        this.updateProfileService = updateProfileService;
        this.customerRepository = customerRepository;
    }

    @GetMapping("/update-profile/{id}")
    public String showUpdateProfilePage(@PathVariable(name = "id") Long customerId, Model model) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found with ID: " + customerId));
        
        model.addAttribute("customerId", customerId);
        model.addAttribute("customer", customer);
        return "/private/customer-update-profile";
    }

    @PostMapping("/update-profile/{id}")
    public String updateProfile(
            @PathVariable(name = "id") Long customerId,
            @RequestParam("fullName") String fullName,
            @RequestParam("phoneNumber") String phoneNumber,
            @RequestParam(value = "avatar", required = false) MultipartFile avatar,
            RedirectAttributes redirectAttributes) {

        try {
            updateProfileService.updateProfile(customerId, fullName, phoneNumber, avatar);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật hồ sơ thành công!");
            return "redirect:/user/update-profile/" + customerId;
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
            return "redirect:/user/update-profile/" + customerId;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi hệ thống: " + e.getMessage());
            return "redirect:/user/update-profile/" + customerId;
        }
    }
}
