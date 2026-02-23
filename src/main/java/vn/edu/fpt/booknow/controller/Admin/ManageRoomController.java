package vn.edu.fpt.booknow.controller.Admin;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import vn.edu.fpt.booknow.entities.Room;
import vn.edu.fpt.booknow.services.ManageRoomServices;

import java.util.List;

@Controller
@RequestMapping(value = "/admin")
public class ManageRoomController {
    final static int ITEM_PER_PAGE = 5;
    private ManageRoomServices manageRoomServices;

    public ManageRoomController(ManageRoomServices roomServices) {
        this.manageRoomServices = roomServices;
    }

    @GetMapping("/dashboard")
    public String adminDashboard() {
        return "private/Admin_dashboard";
    }

    @GetMapping(value = "/list")
    public String listRoom(Model model,
                           @RequestParam(name = "page", required = false, defaultValue = "1") int page) {
        Page<Room> roomlist = manageRoomServices.getAllWithPagination(
                PageRequest.of(page - 1, ManageRoomController.ITEM_PER_PAGE)
        );
        model.addAttribute("rooms", roomlist);
        model.addAttribute("totalRoom", roomlist.getTotalElements());
        model.addAttribute("totalPages", roomlist.getTotalPages());

        return "private/Room_list";
    }

    @GetMapping("/create")
    public String createRoom() {
        return "private/Room_create";
    }

    @GetMapping("/detail")
    public String viewDetailRoom() {
        return "private/Room_Detail";
    }

    @GetMapping("/update")
    public String updateRoom() {
        return "private/Room_update_stt";
    }
}
