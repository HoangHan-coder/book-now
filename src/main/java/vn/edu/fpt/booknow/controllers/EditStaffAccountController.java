package vn.edu.fpt.booknow.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import vn.edu.fpt.booknow.model.dto.UserDetailDTO;
import vn.edu.fpt.booknow.services.admin.EditStaffAccountService;

// Boundary Class (COMET)
// UC-17.x: Edit Staff Account
@Controller
@RequestMapping("/admin/users")
public class EditStaffAccountController {

    private final EditStaffAccountService service;

    public EditStaffAccountController(EditStaffAccountService service) {
        this.service = service;
    }

    // UC-17.x: Show Edit Staff Form
    @GetMapping("/edit")
    public String showEditForm(@RequestParam("userId") Long userId,
                               Model model) {

        UserDetailDTO staff = service.getStaffAccountById(userId);

        model.addAttribute("user", staff);

        return "private/Staff_acc_edit";
    }

    // UC-17.x: Submit Edit Staff Account
    @PostMapping("/edit")
    public String updateStaffAccount(@RequestParam(value = "userId", required = false) Long userId,
                                     @RequestParam(value = "fullName", required = false) String fullName,
                                     @RequestParam(value = "phone", required = false) String phone,
                                     @RequestParam(value = "role", required = false) String role,
                                     @RequestParam(value = "status", required = false) String status) {

        try {
            service.updateStaffAccount(userId, fullName, phone, role, status);

            return "redirect:/admin/users/detail?userId=" + userId + "&userType=STAFF";
        } catch (Exception e) {
            return "redirect:/admin/users/edit?userId=" + userId + "&userType=STAFF";
        }
    }
}