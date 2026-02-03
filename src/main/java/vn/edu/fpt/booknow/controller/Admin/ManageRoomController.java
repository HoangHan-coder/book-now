package vn.edu.fpt.booknow.controller.Admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(value = "/admin")
public class ManageRoomController {

    @GetMapping("/dashboard")
    public String adminDashboard() {
        return "private/Admin_dashboard";
    }

    @RequestMapping(value = "/list")
    public String listRoom() {
        return "private/Room_detail";
    }

    @GetMapping("/create")
    public String createRoom() {
        return "private/Room_create";
    }
}
