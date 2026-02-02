package vn.edu.fpt.booknow.controller.customer;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(value = "/authen")
public class AuthenticationController {

    @GetMapping(value = "/registerEmail")
    public String registerEmail() {
        return "authentication/RegisterWithGoogle";
    }

    @GetMapping(value = "/login")
    public String login() {
        return "authentication/login";
    }

    @GetMapping(value = "/home")
    public String home() {
        return "index";
    }

}
