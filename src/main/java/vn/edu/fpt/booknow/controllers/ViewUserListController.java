package vn.edu.fpt.booknow.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import vn.edu.fpt.booknow.model.dto.UserDTO;
import vn.edu.fpt.booknow.services.ViewUserListService;

import java.util.List;
import java.util.Set;

@Controller
@RequestMapping("/admin")
public class ViewUserListController {

    private final ViewUserListService service;

    public ViewUserListController(ViewUserListService service) {
        this.service = service;
    }

    // 17.1
    @GetMapping("/account_list")
    public String viewUserList(
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search,
            Model model) {
        try {
            String roleParam = (role != null && !role.isBlank()) ? role.toUpperCase() : null;
            Set<String> validRoles = Set.of("ADMIN", "STAFF", "HOUSEKEEPING", "CUSTOMER");

            if (roleParam != null && !validRoles.contains(roleParam)) {
                roleParam = null;
            }

            String statusParam = (status != null && !status.isBlank()) ? status.toUpperCase() : null;
            String keywordParam = (search != null && !search.isBlank()) ? search : null;

            List<UserDTO> users = service.getUserList(roleParam, statusParam, keywordParam);

            model.addAttribute("users", users);

            if (users.isEmpty()) {
                model.addAttribute("message", "Không tìm thấy người dùng nào.");
            }

            return "private/Account_list";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "error-page";
        }
    }
}
