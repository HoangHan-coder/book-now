package vn.edu.fpt.booknow.controllers.client;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import vn.edu.fpt.booknow.model.dto.*;
import vn.edu.fpt.booknow.model.entities.*;
import vn.edu.fpt.booknow.services.FeedbackService;
import vn.edu.fpt.booknow.services.JWTService;
import vn.edu.fpt.booknow.services.RoomService;
import vn.edu.fpt.booknow.services.CustomerService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
public class RoomController {
    private final CustomerService customerService;
    private RoomService roomService;
    private JWTService jwtService;
    private FeedbackService feedbackService;
    public RoomController(RoomService roomService, JWTService jwtService, FeedbackService feedBackService, CustomerService customerService) {
        this.roomService = roomService;
        this.jwtService = jwtService;
        this.feedbackService = feedBackService;
        this.customerService = customerService;
    }

    @GetMapping("/404")
    public String error404() {
        return "404";
    }

    @GetMapping("/detail/{roomIdString}")
    public String detailRoom(@PathVariable String roomIdString,
                             Model model,
                             @RequestParam(value = "preDate", required = false) String preDate,
                             @RequestParam(value = "preSlotId", required = false) Long preSlotId,
                             @CookieValue(name = "Access_token", required = false) String accessToken
    ) {
        // 1. Kiểm tra Access Token
        try {
            Long roomId = Long.parseLong(roomIdString);
            String email = "";
            Customer customer = new Customer();
            List<DetailRoomDTO> roomDetail = roomService.detailRoomService(roomId);
            if (roomDetail.isEmpty()) {
                return "redirect:/404";
            }
            List<Timetable> timetables = roomService.getAllTimeTable();
            List<TimeTableDTO> getSlot = roomService.getSlot(roomId);
            BookingCustomerDTO booking = new BookingCustomerDTO();
            LocalDateTime today = LocalDateTime.now();
            Room room = roomService.findRoom(roomId);
            List<Image> image = roomService.getImgRoom(room);
            Map<String, Object> feedbackData = feedbackService.getRoomFeedbackData(roomId);
            if (accessToken != null && !accessToken.isEmpty()) {
                email = jwtService.extractUserName(accessToken);
                System.out.println(email);
                customer = customerService.findCusByEmail(email);
                booking.setCustomer(customer);
            }
            List<LocalDateTime> weekDates = roomService.getWeekDates(7);
            Set<String> bookedKeys = roomService.getBookedKeys(getSlot, weekDates, timetables);
            Room room1 = roomService.findRoom(roomId);
            booking.setRoom(room1);
            List<String> monthDateStrings = roomService.getNext365Days();
            List<Map<String, Object>> simpleTimetables = roomService.getSimpleTimetables(timetables);
            model.addAttribute("fullName", customer.getFullName());
            model.addAttribute("timeTableJS", simpleTimetables);
            model.addAttribute("monthDates", monthDateStrings);
            model.addAttribute("bookedKeys", bookedKeys);
            model.addAttribute("timeTable", timetables);
            model.addAttribute("weekDates", weekDates);
            model.addAttribute("today", today);
            model.addAttribute("roomDetail", roomDetail);
            model.addAttribute("informBooking", booking);
            model.addAttribute("image", image);
            model.addAttribute("feedbackStats", feedbackData.get("stats"));
            model.addAttribute("feedbackList", feedbackData.get("list"));
            model.addAttribute("preDate", preDate);
            model.addAttribute("preSlotId", preSlotId);
            return "public/DetailRoom";
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return "redirect:/home";
        }
    }

    @PostMapping("/search")
    public String searchPost(@ModelAttribute("search") SearchDTO searchDTO,
                             Model model,
                             @CookieValue(name = "Access_token", required = false) String accessToken) {
        try {
            // Luôn mặc định về trang 0 khi bấm tìm mới
            String email;
            Customer customer = new Customer();            Page<DetailRoomDTO> rooms = roomService.getSearchService(searchDTO, searchDTO.getPage());
            List<Integer> pageNumbers = roomService.getPageNumbers(rooms, 5);
            if (accessToken != null && !accessToken.isEmpty()) {
                email = jwtService.extractUserName(accessToken);
                System.out.println(email);
                customer = customerService.findCusByEmail(email);
                model.addAttribute("fullName", customer.getFullName());
            }
            model.addAttribute("rooms", rooms);
            model.addAttribute("search", searchDTO);
            model.addAttribute("pageNumbers", pageNumbers);
            model.addAttribute("amenities", roomService.getAllAmenity());
            return "public/SearchRoom";
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return "redirect:/home";
        }
    }

    // 2. XỬ LÝ KHI NGƯỜI DÙNG CHUYỂN TRANG (GET)
    @GetMapping("/search")
    public String searchGet(@ModelAttribute("search") SearchDTO searchDTO,
                            @RequestParam(value = "page", defaultValue = "0") int page,
                            Model model) {
        try {
            // Sử dụng tham số 'page' từ URL
            Page<DetailRoomDTO> rooms = roomService.getSearchService(searchDTO, page);
            List<Integer> pageNumbers = roomService.getPageNumbers(rooms, 5);
            model.addAttribute("rooms", rooms);
            model.addAttribute("search", searchDTO);
            model.addAttribute("pageNumbers", pageNumbers);
            model.addAttribute("amenities", roomService.getAllAmenity());
            System.out.println(rooms.getTotalPages() + " Get");
            return "public/SearchRoom";
        } catch (Exception e) {
            return "redirect:/home";
        }
    }

}
