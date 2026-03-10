package vn.edu.fpt.booknow.controllers.client;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.edu.fpt.booknow.model.dto.*;
import vn.edu.fpt.booknow.model.entities.*;
import vn.edu.fpt.booknow.repositories.CustomerRepository;
import vn.edu.fpt.booknow.repositories.FeedBackRepository;
import vn.edu.fpt.booknow.repositories.ImageRepository;
import vn.edu.fpt.booknow.repositories.RoomRepository;
import vn.edu.fpt.booknow.services.BookingService;
import vn.edu.fpt.booknow.services.FeedBackService;
import vn.edu.fpt.booknow.services.JWTService;
import vn.edu.fpt.booknow.services.RoomService;
import vn.edu.fpt.booknow.services.customer.CustomerService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Controller
public class RoomController {

    private final CustomerService customerService;
    private RoomService roomService;
    private BookingService bookingService;
    private JWTService jwtService;
    private FeedBackService feedbackService;

    public RoomController(RoomService roomService, BookingService bookingService, JWTService jwtService, FeedBackService feedBackService, CustomerService customerService) {
        this.roomService = roomService;
        this.bookingService = bookingService;
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
                             @CookieValue(name = "Access_token", required = false) String accessToken
    ) {
        // 1. Kiểm tra Access Token
        try {
            Long roomId = Long.parseLong(roomIdString);
            String email = "";
            Customer customer = new Customer();
            List<RoomDTO> roomDetail = roomService.detailRoomService(roomId);
            if (roomDetail.isEmpty()) {
                return "redirect:/404";
            }
            List<Timetable> timetables = roomService.getAllTimeTable();
            List<TimeTableDTO> getSlot = roomService.getSlot(roomId);
            BookingDTO booking = new BookingDTO();
            List<LocalDateTime> weekDates = new ArrayList<>();
            LocalDateTime today = LocalDateTime.now();
            Room room = roomService.findRoom(roomId);
            List<Image> image = roomService.getImgRoom(room);
            Map<String, Object> feedbackData = feedbackService.getRoomFeedbackData(roomId);
            if (accessToken != null && !accessToken.isEmpty()) {
                email = jwtService.extractUserName(accessToken);
                System.out.println(email);
                customer = customerService.findCustomer(email);
                booking.setCustomer(customer);
            }
            booking.setCustomer(customer);
            for (int i = 0; i < 7; i++) {
                weekDates.add(today.plusDays(i + 1));
            }
            Set<String> bookedKeys = new HashSet<>();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddMM");

            for (TimeTableDTO s : getSlot) {
                String key = s.getDate().format(formatter) + "-" + s.getTimetableId();
                bookedKeys.add(key);
            }
            booking.setRoomId(roomId);
            model.addAttribute("bookedKeys", bookedKeys);
            model.addAttribute("timeTable", timetables);
            model.addAttribute("weekDates", weekDates);
            model.addAttribute("today", today);
            model.addAttribute("roomDetail", roomDetail);
            model.addAttribute("informBooking", booking);
            model.addAttribute("image", image);
            model.addAttribute("feedbackStats", feedbackData.get("stats"));
            model.addAttribute("feedbackList", feedbackData.get("list"));
            return "public/DetailRoom";
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return "redirect:/homepage";
        }
    }

    @PostMapping("/booking/process")
    public String handleBookingRequest(
            @RequestParam("roomId") Long roomId,
            @RequestParam("date") String date, // Sẽ nhận chuỗi "2026-03-01"
            @RequestParam("timetableId") Long timetableId,
            RedirectAttributes redirectAttributes) {

        // Bước này bạn có thể thực hiện kiểm tra DB:
        // 1. Phòng có tồn tại không?
        // 2. Khung giờ này đã bị ai đặt chưa (tránh trường hợp 2 người cùng đặt 1 lúc)?

        // Sau đó truyền dữ liệu sang trang chi tiết thanh toán
        return "redirect:/detail/" + roomId + "?preDate=" + date + "&preSlotId=" + timetableId;
    }

    @PostMapping("/search")
    public String searchPost(@ModelAttribute("search") SearchDTO searchDTO,
                             Model model) {
        try {
            // Luôn mặc định về trang 0 khi bấm tìm mới
            Page<RoomDTO> rooms = roomService.getSearchService(searchDTO, searchDTO.getPage());

            // Tính toán phân trang trực tiếp trong hàm
            int totalPages = rooms.getTotalPages();
            int current = rooms.getNumber();
            int displayRange = 5;
            int start = Math.max(0, current - displayRange / 2);
            int end = Math.min(totalPages - 1, start + displayRange - 1);
            if (end - start + 1 < displayRange) start = Math.max(0, end - displayRange + 1);
            System.out.println(searchDTO.getPage());
            List<Integer> pageNumbers = new ArrayList<>();
            for (int i = start; i <= end; i++) pageNumbers.add(i);

            model.addAttribute("rooms", rooms);
            model.addAttribute("search", searchDTO);
            model.addAttribute("pageNumbers", pageNumbers);
            model.addAttribute("amenities", roomService.getAllAmenity());

            return "public/SearchRoom";
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return "redirect:/homepage";
        }
    }

    // 2. XỬ LÝ KHI NGƯỜI DÙNG CHUYỂN TRANG (GET)
    @GetMapping("/search")
    public String searchGet(@ModelAttribute("search") SearchDTO searchDTO,
                            @RequestParam(value = "page", defaultValue = "0") int page,
                            Model model) {
        try {
            // Sử dụng tham số 'page' từ URL
            Page<RoomDTO> rooms = roomService.getSearchService(searchDTO, page);

            // Tính toán phân trang trực tiếp trong hàm (lặp lại logic)
            int totalPages = rooms.getTotalPages();
            int current = rooms.getNumber();
            int displayRange = 5;
            int start = Math.max(0, current - displayRange / 2);
            int end = Math.min(totalPages - 1, start + displayRange - 1);
            if (end - start + 1 < displayRange) start = Math.max(0, end - displayRange + 1);

            List<Integer> pageNumbers = new ArrayList<>();
            for (int i = start; i <= end; i++) pageNumbers.add(i);

            model.addAttribute("rooms", rooms);
            model.addAttribute("search", searchDTO);
            model.addAttribute("pageNumbers", pageNumbers);
            model.addAttribute("amenities", roomService.getAllAmenity());
            System.out.println(rooms.getTotalPages() + " Get");
            return "public/SearchRoom";
        } catch (Exception e) {
            return "redirect:/homepage";
        }
    }

    @PostMapping("/booking/save")
    public String bookingSave(@ModelAttribute BookingDTO bookingDTO, @RequestParam(value = "cccd_front", required = false) MultipartFile frontImg,
                              @RequestParam(value = "cccd_back", required = false) MultipartFile backImg,
                              @CookieValue(name = "Access_token", required = false) String accessToken,
                              RedirectAttributes redirectAttributes) {


        try {
            if (accessToken == null || accessToken.isEmpty()) {
                return "redirect:/auth/login";
            }
            String rediect = bookingService.saveBooking(bookingDTO, frontImg, backImg, redirectAttributes, accessToken);
            return rediect;

        } catch (Exception e) {
            System.out.println("test");
            System.out.println(e.getMessage());
            return "redirect:/auth/login";
        }
    }
}
