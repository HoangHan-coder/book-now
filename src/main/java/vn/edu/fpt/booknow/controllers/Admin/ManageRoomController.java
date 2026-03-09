package vn.edu.fpt.booknow.controllers.Admin;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.edu.fpt.booknow.entities.Room;
import vn.edu.fpt.booknow.entities.RoomType;
import vn.edu.fpt.booknow.services.AmenityService;
import vn.edu.fpt.booknow.services.ManageRoomServices;
import vn.edu.fpt.booknow.services.RoomTypeService;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping(value = "/admin")
public class ManageRoomController {
    final static int ITEM_PER_PAGE = 5;
    private ManageRoomServices manageRoomServices;
    private RoomTypeService roomTypeService;
    private AmenityService amenityService;

    public ManageRoomController(ManageRoomServices manageRoomServices, RoomTypeService roomTypeService, AmenityService amenityService) {
        this.manageRoomServices = manageRoomServices;
        this.roomTypeService = roomTypeService;
        this.amenityService = amenityService;
    }

    @GetMapping("/dashboard")
    public String adminDashboard() {
        return "private/Admin_dashboard";
    }

    @GetMapping(value = "/list")
    public String listRoom(
            Model model,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String roomNumber) {
        if (page < 1) {
            page = 1;
        }
        Page<Room> roomlist = manageRoomServices.filterRooms(
                status,
                type,
                roomNumber,
                PageRequest.of(page - 1, ITEM_PER_PAGE)
        );

        if (page > roomlist.getTotalPages() && roomlist.getTotalPages() > 0) {
            return "redirect:/admin/list?page=1"
                    + "&status=" + (status == null ? "" : status)
                    + "&type=" + (type == null ? "" : type)
                    + "&roomNumber=" + (roomNumber == null ? "" : roomNumber);
        }

        model.addAttribute("rooms", roomlist);
        model.addAttribute("totalRoom", roomlist.getTotalElements());
        model.addAttribute("totalPages", roomlist.getTotalPages());

        // keep value filter
        model.addAttribute("status", status);
        model.addAttribute("type", type);
        model.addAttribute("roomNumber", roomNumber);
        return "private/Room_list";
    }

    @GetMapping("/create")
    public String createRoom(Model model) {

        model.addAttribute("roomType", roomTypeService.findAll());
        model.addAttribute("allAmenities", amenityService.findAll());
        return "private/Room_create";
    }

    @GetMapping("/detail/{id}")
    public String viewDetailRoom(Model model, @PathVariable("id") Long id) {

        Room room = manageRoomServices.findRoomById(id);

        if (room == null) {
            return "redirect:/admin/list";
        }

        model.addAttribute("room", room);
        return "private/Room_Detail";
    }

    @GetMapping("/update/{id}")
    public String updateSttRoom(Model model, @PathVariable("id") Long id) {
        Room room = manageRoomServices.findRoomById(id);
        model.addAttribute("room", room);
        return "private/Room_update_stt";
    }

    @GetMapping("/edit/{id}")
    public String editRoom(Model model, @PathVariable("id") Long id) {
        Room room = manageRoomServices.findRoomById(id);

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


        model.addAttribute("room", room);
        model.addAttribute("roomType",roomTypeService.findAll());
        model.addAttribute("allAmenities", amenityService.findAll());
        model.addAttribute("roomAmenityIds", roomAmenityIds);
        model.addAttribute("basePrice", room.getRoomType().getBasePrice().longValue());
        model.addAttribute("overPrice", room.getRoomType().getOverPrice().longValue());
        return "private/Room_edit";
    }

    // submit form edit
    @PostMapping("/edit")
    public String editRoomSubmit(

            // ===== ROOM =====
            @RequestParam Long roomId,
            @RequestParam BigDecimal basePrice,
            @RequestParam BigDecimal overPrice,
            @RequestParam String status,
            @RequestParam Long roomTypeId,

            // ===== ROOM TYPE =====
            @RequestParam("roomTypeDescription") String roomTypeDescription,

            // ===== AMENITIES =====
            @RequestParam(value = "amenityIds", required = false) List<Long> amenityIds,
            @RequestParam(value = "newAmenityNames", required = false) List<String> newAmenityNames,

            // ===== NEW IMAGES =====
            @RequestParam(value = "images", required = false) MultipartFile[] images,

            // ===== IMAGE DELETE =====
            @RequestParam(value = "deletedImageIds", required = false) String deletedImageIds
    ) {

        manageRoomServices.editRoom(
                roomId,
                basePrice,
                overPrice,
                status,
                roomTypeId,
                roomTypeDescription,
                amenityIds,
                newAmenityNames,
                images,
                deletedImageIds
        );

        return "redirect:/admin/list";
    }

    @PostMapping("/rooms/create")
    public String createRoom(
            @RequestParam String roomNumber,
            @RequestParam Long roomTypeId,
            @RequestParam Long basePrice,
            @RequestParam Long overPrice,
            @RequestParam String status,
            @RequestParam(required = false) String description,

            @RequestParam(required = false) List<Long> amenityIds,
            @RequestParam( required = false) List<String> newAmenityNames,
            RedirectAttributes redirectAttributes
    ) {

        try {

            manageRoomServices.createRoom(
                    roomNumber,
                    roomTypeId,
                    basePrice,
                    overPrice,
                    status,
                    description,
                    amenityIds,
                    newAmenityNames
            );

            redirectAttributes.addFlashAttribute("successMessage", "Tạo phòng thành công!");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/admin/list";
    }
}
