package vn.edu.fpt.booknow.controllers.staff;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.edu.fpt.booknow.model.entities.Room;
import vn.edu.fpt.booknow.model.entities.RoomStatus;
import vn.edu.fpt.booknow.services.ManageRoomServices;
import vn.edu.fpt.booknow.services.UpdateSttService;

@Controller
@RequestMapping(value = "/staff")
public class ManageSttRoomController {
    private final ManageRoomServices manageRoomServices;
    private final UpdateSttService updateSttService;

    public ManageSttRoomController(ManageRoomServices manageRoomServices,
            UpdateSttService updateSttService) {
        this.manageRoomServices = manageRoomServices;
        this.updateSttService = updateSttService;
    }

    // ================= GET =================
    @GetMapping("/update/{id}")
    public String updateSttRoom(Model model, @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        try {
            Room room = manageRoomServices.findRoomById(id);

            if (room.getStatus() == RoomStatus.INACTIVE) {
                redirectAttributes.addFlashAttribute("errorMessage", "Phòng đã bị xóa");
                return "redirect:/admin/list";
            }

            model.addAttribute("room", room);
            return "private/Room_update_stt";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/staff/rooms/list";
        }
    }

    // ================= POST =================
    @PostMapping("/update")
    public String updateSttSubmit(
            @RequestParam(required = false) Long roomId,
            @RequestParam(required = false) RoomStatus status,
            RedirectAttributes redirectAttributes) {

        try {
            // ✅ Validate input
            if (roomId == null) {
                throw new RuntimeException("Thiếu roomId");
            }

            if (status == null) {
                throw new RuntimeException("Trạng thái không hợp lệ");
            }

            // ✅ Gọi service
            updateSttService.updateRoomStatus(roomId, status);

            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật trạng thái thành công");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/admin/list";
    }
}
