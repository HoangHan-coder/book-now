package vn.edu.fpt.booknow.controllers.staff;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import vn.edu.fpt.booknow.model.entities.Room;
import vn.edu.fpt.booknow.model.entities.RoomStatus;
import vn.edu.fpt.booknow.services.ManageRoomServices;
import vn.edu.fpt.booknow.services.UpdateSttService;

@Controller
@RequestMapping(value = "/staff")
public class ManageSttRoomController {
    private ManageRoomServices manageRoomServices;
    private UpdateSttService updateSttService;

    public ManageSttRoomController(ManageRoomServices manageRoomServices, UpdateSttService updateSttService) {
        this.manageRoomServices = manageRoomServices;
        this.updateSttService = updateSttService;
    }

    @GetMapping("/update/{id}")
    public String updateSttRoom(Model model, @PathVariable("id") Long id) {
        Room room = manageRoomServices.findRoomById(id);
        if (room.getStatus().equals(RoomStatus.DELETED)) {
            return "redirect:/admin/list";
        }
        model.addAttribute("room", room);
        return "private/Room_update_stt";
    }

    @PostMapping("/update")
    public String updateSttSubmit (
            @RequestParam Long roomId,
            @RequestParam RoomStatus status
    ) {
        if (status == null) {
            throw new IllegalArgumentException("Status is required");
        }

        updateSttService.updateRoomStatus(roomId, status);
        return "redirect:/admin/list";
    }
}
