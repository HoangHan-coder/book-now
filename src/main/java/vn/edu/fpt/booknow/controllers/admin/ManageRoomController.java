package vn.edu.fpt.booknow.controllers.admin;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.edu.fpt.booknow.model.dto.DashboardDTO;
import vn.edu.fpt.booknow.model.entities.Room;
import vn.edu.fpt.booknow.model.entities.RoomStatus;
import vn.edu.fpt.booknow.model.entities.RoomType;
import vn.edu.fpt.booknow.exceptions.InternalServerException;
import vn.edu.fpt.booknow.exceptions.ResourceNotFoundException;
import vn.edu.fpt.booknow.services.AmenityService;
import vn.edu.fpt.booknow.services.ManageRoomServices;
import vn.edu.fpt.booknow.services.RoomTypeService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@Controller
@RequestMapping(value = "/staff/")
public class ManageRoomController {
    final static int ITEM_PER_PAGE = 10;
    private ManageRoomServices manageRoomServices;
    private RoomTypeService roomTypeService;
    private AmenityService amenityService;

    @Autowired
    public ManageRoomController(ManageRoomServices manageRoomServices, RoomTypeService roomTypeService,
            AmenityService amenityService) {
        this.manageRoomServices = manageRoomServices;
        this.roomTypeService = roomTypeService;
        this.amenityService = amenityService;
    }

    @GetMapping(value = "rooms/list")
    public String listRoom(
            Model model,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(required = false) RoomStatus status,
            @RequestParam(required = false) Long type,
            @RequestParam(required = false) String roomNumber) {
        if (page < 1) {
            page = 1;
        }
        try {

            Page<Room> roomlist = manageRoomServices.filterRooms(
                    status,
                    type,
                    roomNumber,
                    PageRequest.of(page - 1, ITEM_PER_PAGE));

            if (page > roomlist.getTotalPages() && roomlist.getTotalPages() > 0) {
                return "redirect:/staff/rooms/list?page=1"
                        + "&status=" + (status == null ? "" : status)
                        + "&type=" + (type == null ? "" : type)
                        + "&roomNumber=" + (roomNumber == null ? "" : roomNumber);
            }

            model.addAttribute("rooms", roomlist);
            model.addAttribute("totalRoom", roomlist.getTotalElements());
            model.addAttribute("totalPages", roomlist.getTotalPages());
            model.addAttribute("roomType", roomTypeService.findAll());
            model.addAttribute("hasDeletedRooms",
                    roomlist.getContent().stream().allMatch(r -> r.getStatus() == RoomStatus.INACTIVE));
        } catch (Exception e) {
            throw new InternalServerException("Cannot load room list");
        }
        // keep value filter
        model.addAttribute("status", status);
        model.addAttribute("type", type);
        model.addAttribute("roomNumber", roomNumber);
        return "private/Room_list";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/rooms/delete-rooms")
    @ResponseBody
    public ResponseEntity<?> deleteRooms(@RequestParam List<Long> roomIds) {

        if (roomIds == null || roomIds.isEmpty()) {
            return ResponseEntity.badRequest().body("No rooms selected");
        }
        manageRoomServices.deleteRooms(roomIds);
        return ResponseEntity.ok("Đã xóa thành công");
    }

    @PostMapping("/room/delete/{id}")
    public String softDeleteRoom(@PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        try {
            manageRoomServices.softDeleteRoom(id);
            redirectAttributes.addFlashAttribute("success", "Phòng Đã Ngừng sử dụng");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/staff/rooms/list";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/room/restore/{id}")
    public String restoreRoom(@PathVariable Long id,
            RedirectAttributes redirectAttributes) {
        try {
            manageRoomServices.restoreRoom(id);
            redirectAttributes.addFlashAttribute("success", "Khôi phục phòng thành công");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/staff/rooms/list";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/rooms/create")
    public String createRoom(Model model) {

        model.addAttribute("roomNumber", manageRoomServices.getRoomNumbers());
        model.addAttribute("roomType", roomTypeService.findAll());
        model.addAttribute("allAmenities", amenityService.findAll());
        return "private/Room_create";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/rooms/create")
    public String createRoom(
            @RequestParam String roomNumber,
            @RequestParam Long roomTypeId,
            @RequestParam Long basePrice,
            @RequestParam Long overPrice,
            @RequestParam(required = false) String description,

            @RequestParam(required = false) List<Long> amenityIds,
            @RequestParam(required = false) List<String> newAmenityNames,
            @RequestParam(required = false) List<MultipartFile> newAmenityIcons,

            @RequestParam(required = false) MultipartFile[] images,

            RedirectAttributes redirectAttributes) {
        try {

            manageRoomServices.createRoom(
                    roomNumber,
                    roomTypeId,
                    basePrice,
                    overPrice,
                    description,
                    amenityIds,
                    newAmenityNames,
                    newAmenityIcons,
                    images);
            redirectAttributes.addFlashAttribute("successMessage", "Tạo phòng thành công!");

        } catch (Exception e) {
            throw new InternalServerException("Create room failed: " + e.getMessage());
        }

        return "redirect:/staff/rooms/list";
    }

    @GetMapping("/rooms/detail/{id}")
    public String viewDetailRoom(Model model, @PathVariable("id") Long id) {

        Room room = manageRoomServices.findRoomById(id);

        if (room == null) {
            throw new ResourceNotFoundException("Room not found with id: " + id);
        }

        model.addAttribute("room", room);
        model.addAttribute("isDeleted", room.getStatus() == RoomStatus.INACTIVE);
        return "private/Room_Detail";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/rooms/edit/{id}")
    public String editRoom(Model model, @PathVariable("id") Long id) {
        Room room = manageRoomServices.findRoomById(id);

        if (room == null) {
            throw new ResourceNotFoundException("Room not found with id: " + id);
        }

        if (room.getStatus() == RoomStatus.INACTIVE) {
            return "redirect:/staff/detail/" + id + "?error=deleted";
        }

        if (room.getRoomType() == null) {
            room.setRoomType(new RoomType()); // chống null
        }

        Long basePrice = 0L;
        Long overPrice = 0L;

        if (room.getRoomType().getBasePrice() != null) {
            basePrice = room.getRoomType().getBasePrice().longValue();
        }

        if (room.getRoomType().getOverPrice() != null) {
            overPrice = room.getRoomType().getOverPrice().longValue();
        }

        if (room.getRoomAmenities() == null) {
            room.setRoomAmenities(new ArrayList<>());
        }

        List<Long> roomAmenityIds = room.getRoomAmenities()
                .stream()
                .map(ra -> ra.getAmenity().getAmenityId())
                .toList();

        List<RoomStatus> allowedStatuses = manageRoomServices.getAllowedStatusesWithCurrent(room.getStatus());

        model.addAttribute("room", room);
        model.addAttribute("allowedStatuses", allowedStatuses);
        model.addAttribute("roomType", roomTypeService.findAll());
        model.addAttribute("allAmenities", amenityService.findAll());
        model.addAttribute("roomAmenityIds", roomAmenityIds);
        model.addAttribute("basePrice", room.getRoomType().getBasePrice().longValue());
        model.addAttribute("overPrice", room.getRoomType().getOverPrice().longValue());
        return "private/Room_edit";
    }

    // submit form edit
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/rooms/edit")
    public String editRoomSubmit(

            // ===== ROOM =====
            @RequestParam Long roomId,
            @RequestParam BigDecimal basePrice,
            @RequestParam BigDecimal overPrice,
            @RequestParam RoomStatus status,
            @RequestParam Long roomTypeId,

            // ===== ROOM TYPE =====
            @RequestParam("roomTypeDescription") String roomTypeDescription,

            // ===== AMENITIES =====
            @RequestParam(value = "amenityIds", required = false) List<Long> amenityIds,
            @RequestParam(value = "newAmenityNames", required = false) List<String> newAmenityNames,
            @RequestParam(value = "newAmenityIcons", required = false) List<MultipartFile> newAmenityIcons,

            // ===== NEW IMAGES =====
            @RequestParam(value = "images", required = false) MultipartFile[] images,

            // ===== IMAGE DELETE =====
            @RequestParam(value = "deletedImageIds", required = false) String deletedImageIds,
            RedirectAttributes redirectAttributes) {
        try {

            manageRoomServices.editRoom(
                    roomId,
                    basePrice,
                    overPrice,
                    status,
                    roomTypeId,
                    roomTypeDescription,
                    amenityIds,
                    newAmenityNames,
                    newAmenityIcons,
                    images,
                    deletedImageIds);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật thành công!");
        } catch (IllegalArgumentException e) {
            throw e; // lỗi validate
        } catch (Exception e) {
            throw new InternalServerException("Lỗi hệ thống khi cập nhật phòng");
        }
        return "redirect:/staff/rooms/detail/" + roomId;
    }

}
