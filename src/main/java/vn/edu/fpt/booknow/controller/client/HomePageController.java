package vn.edu.fpt.booknow.controller.client;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import vn.edu.fpt.booknow.entities.Room;
import vn.edu.fpt.booknow.services.RoomService;

import java.util.List;

@Controller
@RequestMapping("/client")
public class HomePageController {
    private RoomService roomService;
    public HomePageController(RoomService roomService ) {
        this.roomService = roomService;
    }
    @GetMapping("/homepage")
    public String HomePage(Model model) {
        List<Room> list = roomService.getAllRoomService();
        model.addAttribute("rooms",list);
        return "public/HomePage";
    }
}
