package vn.edu.fpt.booknow.controller.client;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import vn.edu.fpt.booknow.dto.SearchDTO;

@Controller
public class RoomController {
    @GetMapping("/detail/{roomId}")
    public String detail(@PathVariable Long roomId) {
        System.out.println(roomId);
        return "public/DetailRoom";
    }
    @PostMapping("/search")
    public String search(SearchDTO searchDTO) {
        System.out.println(searchDTO.getAmenity());
        return "public/SearchRoom";
    }
}
