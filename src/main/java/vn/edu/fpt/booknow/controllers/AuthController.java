package vn.edu.fpt.booknow.controllers;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import vn.edu.fpt.booknow.model.entities.Customer;
import vn.edu.fpt.booknow.model.entities.StaffAccount;
import vn.edu.fpt.booknow.services.JWTService;
import vn.edu.fpt.booknow.services.RecaptchaService;
import vn.edu.fpt.booknow.services.customer.CustomerService;
import vn.edu.fpt.booknow.services.staffadmin.StaffAccountService;

@Controller
public class AuthController {

    @Autowired
    private CustomerService customerService;

    @Autowired
    private JWTService jwtService;

    @Autowired
    private StaffAccountService staffAccountService;

    @Autowired
    private RecaptchaService recaptchaService;

    @GetMapping("/admin/login")
    public String loginAdminPanel(Model model,
                                  @RequestParam(name = "error", required = false) String error,
                                  @RequestParam(name = "captchaError", required = false) String captchaError) {
        if (error != null) {
            model.addAttribute("error", "Email hoặc mật khẩu  không chính xác!");
        }
        if (captchaError != null) {
            model.addAttribute("captchaError", "Captcha không hợp lệ!");
        }
        model.addAttribute("staffAccount", new StaffAccount());
        return "public/authentication/login-admin";
    }

    @PostMapping("/admin/login")
    public String loginAdminHandle(@RequestParam(name = "g-recaptcha-response", required = false) String recaptchaResponse,
                                   @ModelAttribute StaffAccount staffAccount,
                                   HttpServletResponse response) {
//        if (recaptchaResponse == null || recaptchaResponse.isEmpty() || !recaptchaService.verify(recaptchaResponse)) {
//            model.addAttribute("errorRecaptcha", "Captcha không hợp lệ!");
//            model.addAttribute("staffAccount", staffAccount);
//            return "public/authentication/login-admin";
//        }
        boolean loginStatus = staffAccountService.verify(staffAccount, response);
        if (!loginStatus) {
            return  "redirect:/admin/login?error";
        }
        return "redirect:/admin/dashboard";

    }

    @GetMapping("/auth/login")
    public String loginCustomerPanel(Model model,
                                     @RequestParam(name = "error", required = false) String error,
                                     @RequestParam(name = "captchaError", required = false) String captchaError) {
        if (error != null) {
            model.addAttribute("error", "Email hoặc mật khẩu  không chính xác!");
        }
        if (captchaError != null) {
            model.addAttribute("captchaError", "Captcha không hợp lệ!");
        }

        model.addAttribute("customer", new Customer());

        return "public/authentication/login-customer";
    }

    @PostMapping("/auth/login")
    public String loginCustomerHandle(@RequestParam(name = "g-recaptcha-response", required = false) String recaptchaResponse,
                                      @ModelAttribute Customer customer,
                                      HttpServletResponse response) {
        boolean loginStatus = customerService.verify(customer, response);
        if (!loginStatus) {
            return  "redirect:/auth/login?error";
        }
        return "redirect:/home";
    }

    @GetMapping("auth/logout")
    public String logoutCustomerHandle(HttpServletResponse response) {
        jwtService.removeCookie(response);
        return "redirect:/auth/login";
    }
}
