package vn.edu.fpt.booknow.controller.customer;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
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
    public String profile(Model model, @PathVariable(name ="id") long id) {
        Customer customer = profileService.profileDetailById( id);
        model.addAttribute("customerProfile", customer);
        return "/private/customer-profile";
    }

}
