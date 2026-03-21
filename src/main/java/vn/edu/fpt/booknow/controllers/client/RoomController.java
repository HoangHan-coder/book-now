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
            System.out.println(roomId + " detailRoomService");
            List<DetailRoomDTO> roomDetail = roomService.detailRoomService(roomId);
            if (roomDetail.isEmpty()) {
                return "redirect:/404";
            }
            List<Timetable> timetables = roomService.getAllTimeTable();
            List<TimeTableDTO> getSlot = roomService.getSlot(roomId);
            BookingCustomerDTO booking = new BookingCustomerDTO();
            List<LocalDateTime> weekDates = new ArrayList<>();
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
            booking.setCustomer(customer);
            for (int i = 0; i < 7; i++) {
                weekDates.add(today.plusDays(i + 1));
            }
            Set<String> bookedKeys = new HashSet<>();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddMM");

            if (!getSlot.isEmpty()) {
                // Nhóm các slot theo BookingId để xử lý các khoảng đặt dài
                Map<Long, List<TimeTableDTO>> bookingsGrouped = getSlot.stream()
                        .filter(s -> s.getBookingId() != null)
                        .collect(Collectors.groupingBy(TimeTableDTO::getBookingId));

                for (List<TimeTableDTO> slotsInBooking : bookingsGrouped.values()) {
                    // Tìm slot bắt đầu và kết thúc của mỗi đơn đặt
                    TimeTableDTO first = slotsInBooking.stream()
                            .min(Comparator.comparing(TimeTableDTO::getDate)
                                    .thenComparing(TimeTableDTO::getTimetableId)).get();

                    TimeTableDTO last = slotsInBooking.stream()
                            .max(Comparator.comparing(TimeTableDTO::getDate)
                                    .thenComparing(TimeTableDTO::getTimetableId)).get();

                    // Chuyển ngày và slot thành một con số tuyến tính để dễ so sánh (ví dụ: ngày * 10 + slotId)
                    // Hoặc đơn giản là lặp qua danh sách weekDates và timetables để kiểm tra
                    for (LocalDateTime d : weekDates) {
                        for (Timetable t : timetables) {
                            // Kiểm tra xem (d, t) có nằm giữa (first.date, first.slotId) và (last.date, last.slotId) không
                            if (roomService.isBetween(d, t.getTimetableId(), first, last)) {
                                bookedKeys.add(d.format(formatter) + "-" + t.getTimetableId());
                            }
                        }
                    }
                }
            }

            Room room1 = roomService.findRoom(roomId);
            booking.setRoom(room1);
            DateTimeFormatter formatterr = DateTimeFormatter.ofPattern("dd/MM/yyyy"); // Dùng "/" để dễ nhìn

            List<String> monthDateStrings = IntStream.range(0, 365)
                    .mapToObj(i -> LocalDate.now().plusDays(i+1).format(formatterr))
                    .collect(Collectors.toList());
            List<Map<String, Object>> simpleTimetables = timetables.stream().map(t -> {
                Map<String, Object> map = new HashMap<>();
                map.put("timetableId", t.getTimetableId());
                map.put("slotName", t.getSlotName());
                return map;
            }).collect(Collectors.toList());
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
                             Model model) {
        try {
            // Luôn mặc định về trang 0 khi bấm tìm mới
            Page<DetailRoomDTO> rooms = roomService.getSearchService(searchDTO, searchDTO.getPage());

            // Tính toán phân trang trực tiếp trong hàm
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
            return "redirect:/home";
        }
    }

}
