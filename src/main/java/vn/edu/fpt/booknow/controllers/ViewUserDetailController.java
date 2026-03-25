package vn.edu.fpt.booknow.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import vn.edu.fpt.booknow.model.dto.UserDetailDTO;
import vn.edu.fpt.booknow.services.ViewUserDetailService;

@Controller
@RequestMapping("/admin/users")
public class ViewUserDetailController {

    private final ViewUserDetailService service;

    public ViewUserDetailController(ViewUserDetailService service) {
        this.service = service;
    }

    // UC-17.2: View User Detail
    @GetMapping("/detail")
    public String viewUserDetail(@RequestParam("userId") String userId,
            @RequestParam("userType") String userType,
            Model model) {
        try {
            UserDetailDTO userDetail = service.getUserDetail(userId, userType);

            model.addAttribute("user", userDetail);

            return "private/Account_detail";
        } catch (Exception e) {
            return "redirect:/admin/account_list";
        }
    }
}