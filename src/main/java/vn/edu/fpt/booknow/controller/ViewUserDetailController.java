package vn.edu.fpt.booknow.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import vn.edu.fpt.booknow.dto.UserDetailDTO;
import vn.edu.fpt.booknow.services.ViewUserDetailService;

@Controller
@RequestMapping("/admin/users")
public class ViewUserDetailController {

    private final ViewUserDetailService service;

    public ViewUserDetailController(ViewUserDetailService service) {
        this.service = service;
    }

    // UC-17.2: View User Detail
    @GetMapping("/{id}")
    public String viewUserDetail(@PathVariable("id") String userId,
                                 @RequestParam("role") String role,
                                 Model model) {

        UserDetailDTO userDetail =
                service.getUserDetail(userId, role);

        model.addAttribute("user", userDetail);

        return "private/Account_detail"; // read-only view
    }
}
