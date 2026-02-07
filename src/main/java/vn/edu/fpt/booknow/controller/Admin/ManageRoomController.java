package vn.edu.fpt.booknow.controller.Admin;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import vn.edu.fpt.booknow.services.RoomServices;

@Controller
@RequestMapping(value = "/admin")
public class ManageRoomController {
    private RoomServices roomServices;

    public ManageRoomController(RoomServices roomServices) {
        this.roomServices = roomServices;
    }

    @GetMapping("/dashboard")
    public String adminDashboard() {
        return "private/Admin_dashboard";
    }

    @GetMapping(value = "/list")
    public String listRoom( @RequestParam(required = false) String status,
                            @RequestParam(required = false) String type,
                            @RequestParam(required = false) String roomNumber,
                            Model model) {
        model.addAttribute("rooms", roomServices.getAll());
        model.addAttribute("rooms",
                roomServices.searchRoom(status, type, roomNumber));
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
