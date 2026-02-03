package vn.edu.fpt.booknow.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class AuthController {
    @GetMapping("/admin/login")
    public String loginAdminPanel() {
        return "public/authencation/login-admin";
    }

    @GetMapping("auth/login")
    public String loginCustomerPanel() {
        return "public/authencation/login-customer";
    }
}
