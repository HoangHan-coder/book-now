package vn.edu.fpt.booknow.controllers.staffadmin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminDashboardController {
    @GetMapping("admin/dashboard")
    public String dasdboardPanel() {
        return "private/admin-dashboard";
    }

}
