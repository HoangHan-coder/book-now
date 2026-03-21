package vn.edu.fpt.booknow.controllers.client;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import vn.edu.fpt.booknow.model.dto.DetailRoomDTO;
import vn.edu.fpt.booknow.model.dto.SearchDTO;
import vn.edu.fpt.booknow.model.entities.*;
import vn.edu.fpt.booknow.services.RoomService;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class HomePageController {
    private RoomService roomService;
    public HomePageController(RoomService roomService ) {
        this.roomService = roomService;
    }
    @GetMapping("/home")
    public String getHomePage(Model model) {
        SearchDTO searchDTO = new SearchDTO();
        Page<DetailRoomDTO> list = roomService.getAllRoomService();
        List<Amenity> amenities = roomService.getAllAmenity();
        List<RoomType> roomType = roomService.getAllRoomType();
        List<Booking> booking = roomService.getAllBooking();
        List<Timetable> timetables = roomService.getAllTimeTable();
        List<DetailRoomDTO> roomAll = roomService.roomAll();
        List<LocalDateTime> weekDates = new ArrayList<>();
        LocalDateTime today = LocalDateTime.now();
        List<Scheduler> schedulers = booking.stream()
                .flatMap(b -> b.getSchedulers().stream()) // 'getSchedulers()' là hàm lấy list scheduler trong entity Booking
                .collect(Collectors.toList());
        List<Map<String, Object>> simpleTimetables = timetables.stream().map(t -> {
            Map<String, Object> map = new HashMap<>();
            map.put("timetableId", t.getTimetableId());
            map.put("slotName", t.getSlotName()); // Đảm bảo getter đúng tên
            map.put("startTime", t.getStartTime().toString());
            map.put("endTime", t.getEndTime().toString());
            return map;
        }).collect(Collectors.toList());
        // Thay vì chỉ lấy 7 ngày, hãy lấy 30 ngày cho vào Model

        Map<String, String> bookedKeys = new HashMap<>();

        for (Scheduler s : schedulers) {
            // Lấy trạng thái từ đơn đặt phòng (Booking) của Scheduler đó
            // Đảm bảo s.getBooking().getBookingStatus() trả về Enum hoặc String (CANCELLED, SUCCESS,...)
            String status = s.getBooking().getBookingStatus().toString();

            // Format Key: RoomID_LocalDate_TimetableID
            String key = s.getBooking().getRoom().getRoomId() + "_" +
                    s.getDate().toLocalDate().toString() + "_" +
                    s.getTimetable().getTimetableId();

            // Lưu vào Map: Key là vị trí slot, Value là trạng thái
            bookedKeys.put(key, status);
        }
        System.out.println(list.getTotalPages());

        for (int i = 0; i < 7; i++) {
            weekDates.add(today.plusDays(i+1));
        }
        model.addAttribute("bookedKeys", bookedKeys);
        model.addAttribute("rooms",list);
        model.addAttribute("search",searchDTO);
        model.addAttribute("amenities", amenities);
        model.addAttribute("roomType", roomType);
        model.addAttribute("booking", booking);
        model.addAttribute("timeTable", simpleTimetables);
        model.addAttribute("today", today);
        model.addAttribute("weekDates", weekDates);
        model.addAttribute("roomAll",roomAll);
        return "public/HomePage";
    }
}
