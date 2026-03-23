package vn.edu.fpt.booknow.controllers.client;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import vn.edu.fpt.booknow.model.dto.DetailRoomDTO;
import vn.edu.fpt.booknow.model.dto.SearchDTO;
import vn.edu.fpt.booknow.model.entities.*;
import vn.edu.fpt.booknow.services.CustomerService;
import vn.edu.fpt.booknow.services.JWTService;
import vn.edu.fpt.booknow.services.RoomService;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class HomePageController {
    private RoomService roomService;
    private JWTService jwtService;
    private CustomerService customerService;

    public HomePageController(RoomService roomService, JWTService jwtService, CustomerService customerService) {
        this.roomService = roomService;
        this.jwtService = jwtService;
        this.customerService = customerService;
    }

    @GetMapping("/home")
    public String getHomePage(Model model,
                              @CookieValue(value = "Access_token", required = false) String token) {

        if (token != null) {

            try {
                Customer customer = customerService.findCusByEmail(jwtService.extractUserName(token));
                if (customer != null) {
                    System.out.println("Name is:" + customer.getFullName());
                    model.addAttribute("fullName", customer.getFullName());
                }

                System.out.println("Name is Null");
            } catch (Exception e) {
                return "redirect:/auth/login";
            }
        }

        SearchDTO searchDTO = new SearchDTO();
        Page<DetailRoomDTO> list = roomService.getAllRoomService();
        List<Amenity> amenities = roomService.getAllAmenity();
        List<RoomType> roomType = roomService.getAllRoomType();
        List<Booking> booking = roomService.getAllBooking();
        List<Timetable> timetables = roomService.getAllTimeTable();
        List<DetailRoomDTO> roomAll = roomService.roomAll();
        LocalDateTime today = LocalDateTime.now();
        List<Scheduler> schedulers = roomService.extractSchedulersFromBookings(booking);
        List<Map<String, Object>> simpleTimetables = roomService.getSimpleTimetables(timetables);
        Map<String, String> bookedKeys = roomService.getBookedStatusMap(schedulers);
        List<LocalDateTime> weekDates = roomService.getWeekDates(7);
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
        return "public/home";
    }
}
