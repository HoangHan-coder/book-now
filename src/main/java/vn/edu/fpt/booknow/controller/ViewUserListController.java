package vn.edu.fpt.booknow.controller;




import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import vn.edu.fpt.booknow.entities.StaffAccount;
import vn.edu.fpt.booknow.services.ViewUserListService;

import java.util.List;

@Controller
public class ViewUserListController {

    private final ViewUserListService service;

    public ViewUserListController(ViewUserListService service) {
        this.service = service;
    }

    //17.1
    @GetMapping("/admin/account_list")
    public String viewUserList(
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String status,
            Model model) {

        // Chuyển chuỗi rỗng thành null và VIẾT HOA để khớp SQL
        String roleParam = (role != null && !role.trim().isEmpty()) ? role.toUpperCase() : null;
        String statusParam = (status != null && !status.trim().isEmpty()) ? status.toUpperCase() : null;

        List<Object> users = service.getUserList(roleParam, statusParam);

        if (users.isEmpty()) {
            model.addAttribute("message", "Không tìm thấy người dùng nào khớp với bộ lọc.");
        }

        model.addAttribute("users", users);
        return "private/Account_list";
    }
}

