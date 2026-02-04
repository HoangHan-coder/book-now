package vn.edu.fpt.booknow.controller.client;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import vn.edu.fpt.booknow.dto.RoomDTO;
import vn.edu.fpt.booknow.dto.SearchDTO;
import vn.edu.fpt.booknow.entities.Booking;
import vn.edu.fpt.booknow.services.RoomService;

import java.util.List;

@Controller
public class RoomController {

    private RoomService roomService;
    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }
    @GetMapping("/detail/{roomId}")
    public String detailRoom(@PathVariable Long roomId, Model model) {
        List<RoomDTO> roomDetail = roomService.detailRoomService(roomId);
        Booking booking = new Booking();
        model.addAttribute("roomDetail",roomDetail);
        model.addAttribute("informBooking",booking);
        return "public/DetailRoom";
    }
    @GetMapping("/search")
    public String search() {
        return "public/SearchRoom";
    }
    @PostMapping("/search")
    public String search(@ModelAttribute SearchDTO searchDTO, Model model) {
        Page<RoomDTO> list = roomService.getSearchService(searchDTO);
        System.out.println(list.getNumberOfElements());
        model.addAttribute("rooms",list);
        return "public/SearchRoom";
    }
    @PostMapping("/infor")
    public String booking(@ModelAttribute Booking booking) {
        roomService.saveBooking(booking);
        return "redirect:/detail/1";
    }
}
