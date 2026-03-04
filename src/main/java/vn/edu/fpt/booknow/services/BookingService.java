package vn.edu.fpt.booknow.services;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.edu.fpt.booknow.model.dto.BookingDTO;
import vn.edu.fpt.booknow.model.dto.WorkShift;
import vn.edu.fpt.booknow.model.entities.*;
import vn.edu.fpt.booknow.repositories.BookingRepository;
import vn.edu.fpt.booknow.repositories.RoomRepository;
import vn.edu.fpt.booknow.repositories.ScheduleRepository;
import vn.edu.fpt.booknow.repositories.TimeTableRepository;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class BookingService {
    private RoomRepository roomRepository;
    private BookingRepository bookingRepository;
    private TimeTableRepository timeTableRepository;
    private ScheduleRepository scheduleRepository;
    private Cloudinary cloudinary;

    public BookingService(BookingRepository bookingRepository, TimeTableRepository timeTableRepository, RoomRepository roomRepository, ScheduleRepository scheduleRepository, Cloudinary cloudinary) {
        this.bookingRepository = bookingRepository;
        this.timeTableRepository = timeTableRepository;
        this.roomRepository = roomRepository;
        this.scheduleRepository = scheduleRepository;
        this.cloudinary = cloudinary;
    }

    public String saveBooking(BookingDTO bookingDTO,
                              MultipartFile frontImg,
                              MultipartFile backImg,
                              RedirectAttributes redirectAttributes) {
        List<WorkShift> allShiftss = new ArrayList<>();
        for (String slot : bookingDTO.getSelectedSlots()) {
            WorkShift shift = this.parseShift(slot);

            // 🔥 KIỂM TRA TẠI ĐÂY: Nếu sai khung giờ sẽ báo lỗi ngay
            String errorMsg = validateWorkShiftTime(shift);

            if (errorMsg != null) {
                // Gửi thông báo lỗi về Toast
                redirectAttributes.addFlashAttribute("toastMessage", errorMsg);
                redirectAttributes.addFlashAttribute("toastType", "error");
                // Quay lại trang chi tiết phòng
                return "redirect:/room/detail/" + bookingDTO.getRoomId();
            }

            allShiftss.add(shift);
        }
        Map<String, Integer> shiftOrder = Map.of("Sáng", 0, "Chiều", 1, "Tối", 2, "Đêm", 3);
        int SHIFTS_PER_DAY = 4;
        ;
        List<WorkShift> allShifts = new ArrayList<>();
        // 1. Chuyển đổi thủ công
        for (String slot : bookingDTO.getSelectedSlots()) {
            allShifts.add(this.parseShift(slot));
        }

        // 2. Sắp xếp thủ công
        Collections.sort(allShifts, new Comparator<WorkShift>() {
            @Override
            public int compare(WorkShift o1, WorkShift o2) {
                int dateCompare = o1.getWorkDate().toLocalDate().compareTo(o2.getWorkDate().toLocalDate());
                if (dateCompare == 0) {
                    int order1 = shiftOrder.get(o1.getShiftType());
                    int order2 = shiftOrder.get(o2.getShiftType());
                    return Integer.compare(order1, order2);
                }
                return dateCompare;
            }
        });

        if (allShifts.isEmpty()) {
            return "Vui lòng chọn slot!!!";
        }

        // 2. 🔥 BƯỚC VALIDATION: Chặn so le trong ngày
        // --- BƯỚC VALIDATION TỔNG HỢP ---

// 1. Kiểm tra cấu trúc giờ của TỪNG ca (Kể cả khi chỉ có 1 ca)
        for (WorkShift shift : allShifts) {
            LocalTime start = shift.getStartTime().toLocalTime();
            LocalTime end = shift.getEndTime().toLocalTime();
            String type = shift.getShiftType();

            boolean isTimeValid = switch (type) {
                case "Sáng" -> start.equals(LocalTime.of(10, 30)) && end.equals(LocalTime.of(13, 30));
                case "Chiều" -> start.equals(LocalTime.of(14, 0)) && end.equals(LocalTime.of(17, 0));
                case "Tối" -> start.equals(LocalTime.of(17, 30)) && end.equals(LocalTime.of(20, 30));
                case "Đêm" -> start.equals(LocalTime.of(21, 0)) && end.equals(LocalTime.of(9, 50));
                default -> false;
            };

            if (!isTimeValid) {
                redirectAttributes.addFlashAttribute("toastMessage", "Ca " + shift.getShiftType() + " không đúng khung giờ!");
                redirectAttributes.addFlashAttribute("toastType", "error");
                return "redirect:/room/detail/" + bookingDTO.getRoomId();
            }
        }

// 2. Kiểm tra tính liên tiếp (Chỉ chạy khi list có >= 2 ca)
        for (int i = 1; i < allShifts.size(); i++) {
            WorkShift prev = allShifts.get(i - 1);
            WorkShift curr = allShifts.get(i);
            if (prev.getWorkDate().toLocalDate().equals(curr.getWorkDate().toLocalDate())) {
                int prevIdx = shiftOrder.get(prev.getShiftType());
                int currIdx = shiftOrder.get(curr.getShiftType());
                if (currIdx != prevIdx + 1) {
                    System.out.println("Ngày " + prev.getWorkDate().toLocalDate() + " không liên tiếp.");
                    redirectAttributes.addFlashAttribute("toastMessage", "Ngày " + prev.getWorkDate().toLocalDate() + " không liên tiếp.");
                    redirectAttributes.addFlashAttribute("toastType", "error");
                    return "redirect:/room/detail/" + bookingDTO.getRoomId();
                }
            }
        }
        // 3. Gom nhóm
        List<List<WorkShift>> bookingGroups = new ArrayList<>();
        List<WorkShift> currentGroup = new ArrayList<>();
        currentGroup.add(allShifts.get(0));

        for (int i = 1; i < allShifts.size(); i++) {
            WorkShift prev = allShifts.get(i - 1);
            WorkShift curr = allShifts.get(i);
            if (isConsecutive(prev, curr, shiftOrder, SHIFTS_PER_DAY)) {
                currentGroup.add(curr);
            } else {
                bookingGroups.add(new ArrayList<>(currentGroup));
                currentGroup.clear();
                currentGroup.add(curr);
            }
        }
        bookingGroups.add(currentGroup);

        // --- LẤY DỮ LIỆU TỪ DB MỘT LẦN DUY NHẤT ĐỂ TỐI ƯU ---
        List<Timetable> timetableList = timeTableRepository.findAll();

//        String urlFront = uploadToCloudinary(frontImg);
//        String urlBack = uploadToCloudinary(backImg);

        // 4. Lưu vào DB
        for (List<WorkShift> group : bookingGroups) {
            WorkShift firstShift = group.get(0);
            WorkShift lastShift = group.get(group.size() - 1);
            // B. TÍNH TỔNG TIỀN CHO ĐƠN NÀY (Dựa trên loại ca)
            BigDecimal totalAmount = BigDecimal.valueOf(calculateTotalAmount(group, bookingDTO.getRoomId()));
            LocalDateTime checkInDate = firstShift.getStartTime();
            LocalDateTime checkOutDate = lastShift.getEndTime();
            if ("Đêm".equals(lastShift.getShiftType())) {
                checkOutDate = checkOutDate.plusDays(1);
            }
// --- DÒNG SOUT KIỂM TRA ---
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            System.out.println("====================================");
            System.out.println("LOG ĐẶT PHÒNG:");
            System.out.println(" - Ca bắt đầu: " + firstShift.getShiftType());
            System.out.println(" - Ca kết thúc: " + lastShift.getShiftType());
            System.out.println(" - CHECK-IN:  " + checkInDate.format(formatter));
            System.out.println(" - CHECK-OUT: " + checkOutDate.format(formatter));
            System.out.println(" - Tổng tiền: " + calculateTotalAmount(group, bookingDTO.getRoomId()));
            System.out.println("====================================");
            // B: TẠO BOOKING
            Booking newBooking = new Booking();
            Customer customer = new Customer();
            customer.setCustomerId(1L);
            newBooking.setCustomer(customer);
            Room room = new Room();
            room.setRoomId(bookingDTO.getRoomId());
            newBooking.setRoom(room);
            newBooking.setCheckInTime(checkInDate);
            newBooking.setCheckOutTime(checkOutDate);
            newBooking.setTotalAmount(totalAmount);
            newBooking.setIdCardFrontUrl("https://res.cloudinary.com/dzlfgmtbc/image/upload/v1771992352/zhygftc1kh85gswqjuz8.png");
            newBooking.setIdCardBackUrl("https://res.cloudinary.com/dzlfgmtbc/image/upload/v1771992129/zrvphumlgu2tscz8hcbk.png");
            newBooking.setBookingStatus("PENDING");
            newBooking.setBookingCode("BK-" + System.currentTimeMillis());
            newBooking.setCreatedAt(LocalDateTime.now());
            newBooking.setNote(bookingDTO.getNote());
            Booking savedBooking = bookingRepository.save(newBooking);

            // C: LƯU SCHEDULER (Thay thế getTimetableIdBySlotName)
            for (WorkShift shift : group) {
                Scheduler scheduler = new Scheduler();
                Booking b = new Booking();
                b.setBookingId(savedBooking.getBookingId());
                scheduler.setBooking(b);
                scheduler.setDate(shift.getWorkDate());

                // 🚩 TÌM TIMETABLE ID THỦ CÔNG BẰNG FOREACH
                Timetable tt = new Timetable();

                Long timetableId = null;
                for (Timetable item : timetableList) {
                    if (item.getSlotName().contains(shift.getShiftType())) {
                        timetableId = item.getTimetableId();
                        break;
                    }
                }

                if (timetableId == null) throw new RuntimeException("Lỗi ca!");

                tt.setTimetableId(timetableId);
                scheduler.setTimetable(tt);

                scheduleRepository.save(scheduler);

                System.out.println("   + Đã lưu ca: " + shift.getShiftType() + " (ID: " + timetableId + ")");
            }
        }
        redirectAttributes.addFlashAttribute("toastMessage", "Đặt phòng thành công!");
        redirectAttributes.addFlashAttribute("toastType", "success");
        return "redirect:/homepage"; // Thành công thì về trang chủ
    }

    private String validateWorkShiftTime(WorkShift shift) {
        LocalDate today = LocalDate.now();
        LocalDate workDate = shift.getWorkDate().toLocalDate();
        System.out.println(today);
        // 1. Kiểm tra ngày (Chỉnh sửa: Chỉ cho phép đặt từ ngày mai)
        // Nếu ngày chọn KHÔNG SAU ngày hôm nay (isAfter(today) == false) -> Báo lỗi
        if (!workDate.isAfter(today)) {
            return "❌ LỖI: Bạn chỉ có thể đặt phòng bắt đầu từ ngày mai (" + today.plusDays(1) + ")!";
        }

        // Chặn quá 7 ngày kể từ ngày mai
        if (workDate.isAfter(today.plusDays(7))) {
            return "❌ LỖI: Chỉ được phép đặt phòng trong vòng 7 ngày tới!";
        }

        // 2. Kiểm tra khung giờ (Giữ nguyên logic của bạn)
        LocalTime start = shift.getStartTime().toLocalTime();
        LocalTime end = shift.getEndTime().toLocalTime();
        String type = shift.getShiftType();

        boolean isValidTime = switch (type) {
            case "Sáng"  -> start.equals(LocalTime.of(10, 30)) && end.equals(LocalTime.of(13, 30));
            case "Chiều" -> start.equals(LocalTime.of(14, 0))  && end.equals(LocalTime.of(17, 0));
            case "Tối"   -> start.equals(LocalTime.of(17, 30)) && end.equals(LocalTime.of(20, 30));
            case "Đêm"   -> start.equals(LocalTime.of(21, 0))  && end.equals(LocalTime.of(9, 50));
            default -> false;
        };

        if (!isValidTime) {
            return "❌ LỖI: Ca " + type + " có giờ (" + start + " - " + end + ") không đúng quy định!";
        }

        return null;
    }
    /**
     * Kiểm tra xem ca hiện tại có tiếp nối ngay lập tức sau ca trước không
     */
    private boolean isConsecutive(WorkShift prev, WorkShift curr, Map<String, Integer> order, int totalShifts) {
        long prevDateValue = prev.getWorkDate().toLocalDate().toEpochDay();
        long currDateValue = curr.getWorkDate().toLocalDate().toEpochDay();

        int prevOrder = order.get(prev.getShiftType());
        int currOrder = order.get(curr.getShiftType());

        // Tính "Chỉ số ca tuyệt đối" để so sánh
        // Công thức: (Ngày * 4) + Thứ tự ca
        long prevAbsoluteIndex = (prevDateValue * totalShifts) + prevOrder;
        long currAbsoluteIndex = (currDateValue * totalShifts) + currOrder;

        // Nếu hiệu số bằng 1 thì là liên tiếp (không có gap)
        return (currAbsoluteIndex - prevAbsoluteIndex) == 1;
    }

    private Long calculateTotalAmount(List<WorkShift> group, Long roomId) {
        // 1. Lấy thông tin giá từ RoomType thông qua roomId
        // Giả sử bạn có roomRepository để lấy thông tin giá (image_b38224.png)
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phòng"));

        BigDecimal basePrice = room.getRoomType().getBasePrice();
        BigDecimal overPrice = room.getRoomType().getOverPrice();

        int total = 0;

        // 2. Duyệt từng ca trong nhóm để cộng dồn tiền
        for (WorkShift shift : group) {
            if ("Đêm".equals(shift.getShiftType())) {
                total += overPrice.intValue();
            } else {
                total += basePrice.intValue();
            }
        }

        return (long) total;
    }

    public WorkShift parseShift(String input) {
        // 1. Parse Ngày (dd/MM/yyyy)
        String datePart = input.substring(0, 5); // Ví dụ: "25/02"
        String fullDateStr = datePart + "/" + LocalDate.now().getYear();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDate baseDate = LocalDate.parse(fullDateStr, dateFormatter);

        // 2. Parse Khung giờ (HHhmm)
        String timePart = input.substring(input.indexOf("(") + 1, input.indexOf(")")); // "09h20 - 10h20"
        String[] times = timePart.split(" - ");

        // Chuyển "09h20" -> "09:20" để parse LocalTime
        LocalTime startT = LocalTime.parse(times[0].replace("h", ":"));
        LocalTime endT = LocalTime.parse(times[1].replace("h", ":"));

        // 3. Chuyển tất cả sang LocalDateTime
        LocalDateTime startDateTime = baseDate.atTime(startT);
        LocalDateTime endDateTime = baseDate.atTime(endT);

        // logic ca Đêm: Nếu giờ kết thúc nhỏ hơn giờ bắt đầu (ví dụ 21h -> 09h sáng mai)
        if (endT.isBefore(startT)) {
            endDateTime = endDateTime.plusDays(1);
        }

        // 4. Xác định loại ca
        String type = "";
        if (input.contains("Sáng")) type = "Sáng";
        else if (input.contains("Chiều")) type = "Chiều";
        else if (input.contains("Tối")) type = "Tối";
        else if (input.contains("Đêm")) type = "Đêm";

        // Trả về WorkShift với 3 tham số LocalDateTime và 1 String
        // Constructor: WorkShift(LocalDateTime workDate, LocalDateTime startTime, LocalDateTime endTime, String type)
        return new WorkShift(baseDate.atStartOfDay(), startDateTime, endDateTime, type);
    }

    private String uploadToCloudinary(MultipartFile file) {
        try {
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());
            return uploadResult.get("url").toString(); // Trả về link ảnh công khai
        } catch (IOException e) {
            throw new RuntimeException("Lỗi upload ảnh lên Cloudinary: " + e.getMessage());
        }
    }

}
