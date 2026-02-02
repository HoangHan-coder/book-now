package vn.edu.fpt.booknow.controller.client;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import vn.edu.fpt.booknow.dto.RoomDTO;
import vn.edu.fpt.booknow.dto.SearchDTO;
import vn.edu.fpt.booknow.entities.Room;
import vn.edu.fpt.booknow.services.RoomService;

import java.util.List;

@Controller
public class HomePageController {
    private RoomService roomService;
    public HomePageController(RoomService roomService ) {
        this.roomService = roomService;
    }
    @GetMapping("/homepage")
    public String HomePage(Model model) {
        SearchDTO searchDTO = new SearchDTO();
        Page<RoomDTO> list = roomService.getAllRoomService();
        model.addAttribute("rooms",list);
        model.addAttribute("search",searchDTO);
        return "public/HomePage";
    }
}
