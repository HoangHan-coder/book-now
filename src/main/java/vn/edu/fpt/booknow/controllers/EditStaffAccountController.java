package vn.edu.fpt.booknow.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.edu.fpt.booknow.model.dto.UserDetailDTO;
import vn.edu.fpt.booknow.services.EditStaffAccountService;

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
    public String updateStaffAccount(
            @RequestParam Long userId,
            @RequestParam String fullName,
            @RequestParam String phone,
            @RequestParam String role,
            @RequestParam String status,
            @RequestParam(value = "newPassword", required = false) String newPassword,
            @RequestParam(value = "confirmNewPassword", required = false) String confirmNewPassword,
            @RequestParam(value = "avatar", required = false) MultipartFile avatar,
            RedirectAttributes redirectAttributes) {

        try {

            service.updateStaffAccount(
                    userId,
                    fullName,
                    phone,
                    role,
                    status,
                    newPassword,
                    confirmNewPassword,
                    avatar);

            // gửi message
            redirectAttributes.addFlashAttribute("successMessage",
                    "Cập nhật tài khoản thành công!");

            return "redirect:/admin/users/detail?userId=" + userId + "&userType=" + role;

        } catch (Exception e) {

            redirectAttributes.addFlashAttribute("errorMessage",
                    "Cập nhật thất bại!");

            return "redirect:/admin/users/edit?userId=" + userId;
        }
    }
}