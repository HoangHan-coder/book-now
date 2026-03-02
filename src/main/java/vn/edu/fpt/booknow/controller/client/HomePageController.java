package vn.edu.fpt.booknow.controller.client;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import vn.edu.fpt.booknow.dto.RoomDTO;
import vn.edu.fpt.booknow.dto.SearchDTO;
import vn.edu.fpt.booknow.entities.*;
import vn.edu.fpt.booknow.services.RoomService;

import java.time.LocalDateTime;
import java.util.*;

@Controller
public class HomePageController {
    private RoomService roomService;
    public HomePageController(RoomService roomService ) {
        this.roomService = roomService;
    }
    @GetMapping("/homepage")
    public String getHomePage(Model model) {
        SearchDTO searchDTO = new SearchDTO();
        Page<RoomDTO> list = roomService.getAllRoomService();
        List<Amenity> amenities = roomService.getAllAmenity();
        List<RoomType> roomType = roomService.getAllRoomType();
        List<Booking> booking = roomService.getAllBooking();
        List<Timetable> timetables = roomService.getAllTimeTable();
        List<RoomDTO> roomAll = roomService.roomAll();
        List<LocalDateTime> weekDates = new ArrayList<>();
        LocalDateTime today = LocalDateTime.now();
        List<Scheduler> schedulers = roomService.schedulers(); // Giả sử bạn lấy từ service
        Set<String> bookedKeys = new HashSet<>();

        for (Scheduler s : schedulers) {
            // Format: RoomID_LocalDate_TimetableID
            // Lưu ý: s.getDate() trả về LocalDateTime nên cần lấy toLocalDate()
            String key = s.getBooking().getRoom().getRoomId() + "_" +
                    s.getDate().toLocalDate().toString() + "_" +
                    s.getTimetable().getTimetableId();
            bookedKeys.add(key);
        }


        for (int i = 0; i < 7; i++) {
            weekDates.add(today.plusDays(i+1));
        }
        System.out.println(roomAll.size());
        model.addAttribute("bookedKeys", bookedKeys);
        model.addAttribute("rooms",list);
        model.addAttribute("search",searchDTO);
        model.addAttribute("amenities", amenities);
        model.addAttribute("roomType", roomType);
        model.addAttribute("booking", booking);
        model.addAttribute("bookedKeys", bookedKeys);
        model.addAttribute("timeTable", timetables);
        model.addAttribute("today", today);
        model.addAttribute("weekDates", weekDates);
        model.addAttribute("roomAll",roomAll);
        return "public/HomePage";
    }
}
