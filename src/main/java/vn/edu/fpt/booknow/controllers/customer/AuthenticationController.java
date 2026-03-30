package vn.edu.fpt.booknow.controllers.customer;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import vn.edu.fpt.booknow.model.entities.Customer;
import vn.edu.fpt.booknow.services.AuthService;
import vn.edu.fpt.booknow.services.customer.OtpService;

import java.util.concurrent.TimeUnit;



@Controller
@RequestMapping(value = "/auth")
public class AuthenticationController {

    private final RedisTemplate<String, Integer> redisTemplateToSaveInt;
    private final RedisTemplate<String, Object> redisTemplateObj;
    private static final long OTP_EXPIRE = 1;
    private final OtpService otpService;
    private final AuthService authService;

    @Autowired
    public AuthenticationController(RedisTemplate<String, Integer> redisTemplateToSaveInt, RedisTemplate<String, Object> redisTemplateObj, OtpService otpService, AuthService authService) {
        this.redisTemplateToSaveInt = redisTemplateToSaveInt;
        this.redisTemplateObj = redisTemplateObj;
        this.otpService = otpService;
        this.authService = authService;
    }

    @GetMapping(value = "/registerEmail")
    public String registerEmail() {
        return "authentication/RegisterWithGoogle";
    }


//    @GetMapping(value = "/home")
//    public String home() {
//        return "index";
//    }

    @GetMapping(value = "/otp")
    public String otp(@RequestParam(name = "email") String email, Model model) {
        boolean flag = redisTemplateObj.hasKey("OTP:" + email);
        String key = "OTP";
        System.out.println(flag);
        System.out.println(email);
        if (email == null || email.isBlank()){
            model.addAttribute("error", "email is not null");
            return "authentication/RegisterWithGoogle";
        }

        if (flag) {
            int count = redisTemplateToSaveInt.opsForValue().get(key).intValue();
            count++;
            redisTemplateToSaveInt.opsForValue()
                    .set("OTP", count, OTP_EXPIRE, TimeUnit.MINUTES);
            System.out.println(count);
            if (count >= 2){
                model.addAttribute("otp", "please cannot spam otp");
                model.addAttribute("email", email);
                return "authentication/otp";
            }
        }
        otpService.sendOtp(email);
        model.addAttribute("email", email);
        return "authentication/otp";
    }

    @PostMapping(value = "/verifiedOtp")
    public String verifiedOtp(@RequestParam(name = "email") String email, @RequestParam(name = "otp") String otp, Model model) {
        System.out.println("running verufiedOtp");

        if (otpService.verifyOtp(email,otp)){
        model.addAttribute("email",email);
        return "authentication/registerForm";
        } else {
        model.addAttribute("email",email);
        return "authentication/otp";
        }

    }

    @PostMapping(value = "/registerForm")
    public String register(@RequestParam(name = "fullName") String name, @RequestParam(name = "phone") String phone, @RequestParam(name = "password") String password, @RequestParam(name = "confirmPassword") String confirmPass, @RequestParam(name = "email") String email, Model model) {
        System.out.println("form running....");
        System.out.println(email);
        String path = "authentication/login";

        if (name.isEmpty() || phone.isEmpty() || password.isEmpty()) {

            model.addAttribute("messageFields", "Please fill all the fields");
            return "authentication/registerForm";

        }

        if (password.length() < 6) {

            model.addAttribute("messagePassword", "Password too short, password should be at least 6 characters");
            return "authentication/registerForm";

        }

        if (!name.matches("^[\\p{L}\\s]+$")) {

            model.addAttribute("messageName", "Name is invalid, name should contain letters and space");
            return "authentication/registerForm";
        }

        if (!(phone.matches("^[0-9]{10}$"))) {
            model.addAttribute("messagePhone", "Phone number is invalid");
            return "authentication/registerForm";
        }

        if (!(confirmPass.equals(password))) {
            model.addAttribute("messageConfirm", "Confirm Password is not equal to password!");
            return "authentication/registerForm";
        }


         Customer cus = authService.Register(email, name, password, phone);
        System.out.println("customer is " + cus);
         if(cus == null){
             model.addAttribute("messageEmail", "Email is duplicated");
             return "authentication/registerForm";
         } else {
             return "redirect:/auth/login";
         }



    }

}
