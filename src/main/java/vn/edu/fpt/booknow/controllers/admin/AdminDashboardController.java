package vn.edu.fpt.booknow.controllers.admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminDashboardController {
    @GetMapping("admin/dashboard")
    public String dashboardPanel() {
        return "private/admin-dashboard";
    }

}
