package vn.edu.fpt.booknow.controllers;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.edu.fpt.booknow.model.dto.StaffAccountCreateDTO;
import vn.edu.fpt.booknow.services.CreateStaffAccountService;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin")
public class CreateStaffAccountController {

    private final CreateStaffAccountService service;

    public CreateStaffAccountController(CreateStaffAccountService service) {
        this.service = service;
    }

    // UC-17.X: Open Create Staff Account Page
    @GetMapping("/create_staff_account")
    public String showCreateForm(Model model) {

        model.addAttribute("staffDTO", new StaffAccountCreateDTO());

        return "private/Staff_acc_create";
    }

    // UC-17.X: Submit Create Staff Account
    @PostMapping("/create_staff_account")
    public String createStaffAccount(
            @ModelAttribute("staffDTO") StaffAccountCreateDTO dto,
            RedirectAttributes redirectAttributes,
            Model model) {

        try {

            service.createStaffAccount(dto);

            redirectAttributes.addFlashAttribute(
                    "success",
                    "Tạo tài khoản thành công!");

            return "redirect:/admin/account_list";

        } catch (RuntimeException e) {

            model.addAttribute("error", e.getMessage());

            return "private/Staff_acc_create";
        }
    }
}