package vn.edu.fpt.booknow.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.edu.fpt.booknow.services.ManageUserStatusService;

@Controller
@RequestMapping("/admin/users")
public class ManageUserStatusController {

    private final ManageUserStatusService manageUserStatusService;

    public ManageUserStatusController(ManageUserStatusService manageUserStatusService) {
        this.manageUserStatusService = manageUserStatusService;
    }

    // UC-17.3: Inactivate / Reactivate User Account
    @PostMapping("/change-status")
    public String updateUserStatus(@RequestParam Long userId,
                                   @RequestParam String userType,
                                   @RequestParam String status,
                                   RedirectAttributes redirectAttributes) {

        try {
            manageUserStatusService.changeUserStatus(userId, userType, status);
            redirectAttributes.addFlashAttribute("successMessage", "User status updated successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to update user status.");
        }

        System.out.println("POST CALLED: " + userId + " - " + userType + " - " + status);

        // Refresh User Detail screen
        return "redirect:/admin/users/detail?userId=" + userId + "&userType=" + userType;


    }
}