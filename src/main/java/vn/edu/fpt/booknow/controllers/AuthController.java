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
import vn.edu.fpt.booknow.services.MailService;
import vn.edu.fpt.booknow.services.RecaptchaService;
import vn.edu.fpt.booknow.services.customer.CustomerService;

import java.util.HashMap;
import java.util.Map;


@Controller
public class AuthController {

    @Autowired
    private CustomerService customerService;

    @Autowired
    private JWTService jwtService;


//    @Autowired
//    private RecaptchaService recaptchaService;
//    private RecaptchaService recaptchaService;




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


//    // reset password
//    @GetMapping("/forgot-password")
//    public String forgotPasswordPanel() {
//        return "Forgot_Password_Email";
//    }
//
//    @PostMapping("/forgot-password")
//    public String forgotPasswordHandle(@RequestParam("email") String email) {
//        String otp = String.valueOf(new Random().nextInt(899999) + 100000);
//        System.out.println(otp);
//        if (!otps.containsKey(email)) {
//            otps.put(email,otp);
//            mailService.sendOtp(email, otp);
//        }
//        return "Forgot_Password_OTP";
//    }


}
