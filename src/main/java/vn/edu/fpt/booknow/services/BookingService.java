package vn.edu.fpt.booknow.services;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.edu.fpt.booknow.model.dto.BookingDTO;
import vn.edu.fpt.booknow.model.dto.WorkShift;
import vn.edu.fpt.booknow.model.entities.*;
import vn.edu.fpt.booknow.repositories.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class BookingService {
    private RoomRepository roomRepository;
    private BookingRepository bookingRepository;
    private TimeTableRepository timeTableRepository;
    private ScheduleRepository scheduleRepository;
    private Cloudinary cloudinary;
    private CustomerRepository customerRepository;
    private JWTService jwtService;
    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();
    public BookingService(BookingRepository bookingRepository,
                          TimeTableRepository timeTableRepository,
                          RoomRepository roomRepository,
                          ScheduleRepository scheduleRepository,
                          CustomerRepository customerRepository, Cloudinary cloudinary,
                          JWTService jwtService) {
        this.bookingRepository = bookingRepository;
        this.timeTableRepository = timeTableRepository;
        this.roomRepository = roomRepository;
        this.scheduleRepository = scheduleRepository;
        this.cloudinary = cloudinary;
        this.customerRepository = customerRepository;
        this.jwtService = jwtService;
    }

    private Bucket createNewBucket() {
        return Bucket.builder()
                .addLimit(Bandwidth.classic(1, Refill.intervally(1, Duration.ofMinutes(1))))
                .build();
    }

    public String saveBooking(BookingDTO bookingDTO,
                              MultipartFile frontImg,
                              MultipartFile backImg,
                              RedirectAttributes redirectAttributes,
                              String accessToken, Model model) {

        // 1. Kiểm tra Rate Limit
        String username = jwtService.extractUserName(accessToken);
//        if (isRateLimited(username)) {
//            return setErrorMessage(redirectAttributes, "Thao tác quá nhanh! Vui lòng đợi 5 phút.", bookingDTO.getRoomId());
//        }
        // 2. Parse và Validate các ca (Shifts)
        List<WorkShift> allShifts;
        try {
            allShifts = processAndValidateShifts(bookingDTO.getSelectedSlots(), bookingDTO.getRoomId(), redirectAttributes);
        } catch (IllegalArgumentException e) {
            return setErrorMessage(redirectAttributes, e.getMessage(), bookingDTO.getRoomId());
        }

        // 3. Gom nhóm các ca liên tiếp
        List<List<WorkShift>> bookingGroups = groupConsecutiveShifts(allShifts);

        // 4. Upload ảnh và Lưu vào DB
        // (Trong thực tế, nên dùng link từ Cloudinary, ở đây tôi giữ logic hardcode link của bạn để chạy được ngay)
        saveBookingGroupsToDatabase(bookingGroups, bookingDTO, username, redirectAttributes, frontImg, backImg);

        redirectAttributes.addFlashAttribute("toastMessage", "Đặt phòng thành công!");
        redirectAttributes.addFlashAttribute("toastType", "success");
        model.addAttribute("bookingDTO", bookingDTO);
        return "redirect:/payment";
    }

    // --- CÁC HÀM ĐÃ TÁCH (Dễ dàng Unit Test) ---

    /**
     * Hàm này hoàn toàn có thể Unit Test vì nó xử lý logic nghiệp vụ thuần túy
     */
    public List<WorkShift> processAndValidateShifts(List<String> selectedSlots, Long roomId, RedirectAttributes redirectAttributes) {
        if (selectedSlots == null || selectedSlots.isEmpty()) {
            throw new IllegalArgumentException("Vui lòng chọn ít nhất một slot!");
        }

        List<WorkShift> shifts = new ArrayList<>();
        for (String slot : selectedSlots) {
            WorkShift shift = parseShift(slot);
            try {
                validateAndCalculate(shift.getWorkDate(), shift.getStartTime(), shift.getEndTime(), shift.getShiftType(), roomId);

                // Tiếp tục xử lý lưu DB
            } catch (IllegalArgumentException e) {
                String errorMsg = e.getMessage(); // Đây chính là String thông báo lỗi bạn muốn
                redirectAttributes.addFlashAttribute("toastMessage", errorMsg);
            }
            shifts.add(shift);
        }

        // Sắp xếp
        Map<String, Integer> shiftOrder = Map.of("Sáng", 0, "Chiều", 1, "Tối", 2, "Đêm", 3);
        shifts.sort((o1, o2) -> {
            int dateCompare = o1.getWorkDate().toLocalDate().compareTo(o2.getWorkDate().toLocalDate());
            return (dateCompare != 0) ? dateCompare : Integer.compare(shiftOrder.get(o1.getShiftType()), shiftOrder.get(o2.getShiftType()));
        });

        // Kiểm tra tính liên tiếp trong cùng một ngày (nếu có)
        for (int i = 1; i < shifts.size(); i++) {
            WorkShift prev = shifts.get(i - 1);
            WorkShift curr = shifts.get(i);
            if (prev.getWorkDate().toLocalDate().equals(curr.getWorkDate().toLocalDate())) {
                if (shiftOrder.get(curr.getShiftType()) != shiftOrder.get(prev.getShiftType()) + 1) {
                    throw new IllegalArgumentException("Ngày " + prev.getWorkDate().toLocalDate() + " các ca chọn không liên tiếp.");
                }
            }
        }
        return shifts;
    }

    private List<List<WorkShift>> groupConsecutiveShifts(List<WorkShift> allShifts) {
        Map<String, Integer> shiftOrder = Map.of("Sáng", 0, "Chiều", 1, "Tối", 2, "Đêm", 3);
        List<List<WorkShift>> groups = new ArrayList<>();
        List<WorkShift> currentGroup = new ArrayList<>();
        currentGroup.add(allShifts.get(0));

        for (int i = 1; i < allShifts.size(); i++) {
            if (isConsecutive(allShifts.get(i - 1), allShifts.get(i), shiftOrder, 4)) {
                currentGroup.add(allShifts.get(i));
            } else {
                groups.add(new ArrayList<>(currentGroup));
                currentGroup.clear();
                currentGroup.add(allShifts.get(i));
            }
        }
        groups.add(currentGroup);
        return groups;
    }

    private boolean isRateLimited(String username) {
        Bucket bucket = cache.computeIfAbsent(username, k -> createNewBucket());
        return !bucket.tryConsume(1);
    }

    private String setErrorMessage(RedirectAttributes ra, String msg, Long roomId) {
        ra.addFlashAttribute("toastMessage", msg);
        ra.addFlashAttribute("toastType", "error");
        return "redirect:/detail/" + roomId;
    }

    private void saveBookingGroupsToDatabase(List<List<WorkShift>> bookingGroups,
                                             BookingDTO bookingDTO,
                                             String email,
                                             RedirectAttributes redirectAttributes, MultipartFile frontImg,
                                             MultipartFile backImg){
        List<Timetable> timetableList = timeTableRepository.findAll();
        Customer customer = customerRepository.getCustomerByEmail(email);

        for (List<WorkShift> group : bookingGroups) {
            WorkShift firstShift = group.get(0);
            WorkShift lastShift = group.get(group.size() - 1);
            // B. TÍNH TỔNG TIỀN CHO ĐƠN NÀY (Dựa trên loại ca)
            BigDecimal totalAmount = calculateTotalAmount(group, bookingDTO.getRoomId(), redirectAttributes);
            LocalDateTime checkInDate = firstShift.getStartTime();
            LocalDateTime checkOutDate = lastShift.getEndTime();
            if ("Đêm".equals(lastShift.getShiftType())) {
                checkOutDate = checkOutDate.plusDays(1);
            }
            String front = uploadToCloudinary(frontImg);
            String back = uploadToCloudinary(backImg);
// --- DÒNG SOUT KIỂM TRA ---
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            System.out.println("====================================");
            System.out.println("LOG ĐẶT PHÒNG:");
            System.out.println(" - Ca bắt đầu: " + firstShift.getShiftType());
            System.out.println(" - Ca kết thúc: " + lastShift.getShiftType());
            System.out.println(" - CHECK-IN:  " + checkInDate.format(formatter));
            System.out.println(" - CHECK-OUT: " + checkOutDate.format(formatter));
            System.out.println(" - Tổng tiền: " + calculateTotalAmount(group, bookingDTO.getRoomId(), redirectAttributes));
            System.out.println("====================================");
            // B: TẠO BOOKING
            Booking newBooking = new Booking();
            newBooking.setCustomer(customer);
            Room room = new Room();
            room.setRoomId(bookingDTO.getRoomId());
            newBooking.setRoom(room);
            newBooking.setCheckInTime(checkInDate);
            newBooking.setCheckOutTime(checkOutDate);
            newBooking.setTotalAmount(totalAmount);
//            newBooking.setIdCardFrontUrl("https://res.cloudinary.com/dzlfgmtbc/image/upload/v1771992352/zhygftc1kh85gswqjuz8.png");
//            newBooking.setIdCardBackUrl("https://res.cloudinary.com/dzlfgmtbc/image/upload/v1771992129/zrvphumlgu2tscz8hcbk.png");
            newBooking.setIdCardFrontUrl(front);
            newBooking.setIdCardBackUrl(back);
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

    }
    // Unit Test
    protected BigDecimal validateAndCalculate(
            LocalDateTime workDatee,
            LocalDateTime startTime, LocalDateTime endTime,
            String shiftType, Long roomId) {
        Room room = roomRepository.getByRoomId(roomId);
        if (room == null) {
            System.out.println("❌ Không tìm thấy phòng ID: " + roomId);
            throw new IllegalArgumentException("Không tìm thấy phòng ID: " + roomId);
        }
        LocalDate today = LocalDate.now();
        LocalDate workDate = workDatee.toLocalDate();
        BigDecimal basePrice = room.getBasePrice();
        BigDecimal overPrice = room.getOverPrice();
        // Kiểm tra ngày
        if (!workDate.isAfter(today)) {
            System.out.println("❌ LỖI: Chỉ có thể đặt từ ngày mai!");
            throw new IllegalArgumentException("❌ LỖI: Chỉ có thể đặt từ ngày mai (" + today.plusDays(1) + ")!");
        }
        if (workDate.isAfter(today.plusDays(7))) {
            System.out.println("❌ LỖI: Chỉ được đặt trong vòng 7 ngày tới!");
            throw new IllegalArgumentException("❌ LỖI: Chỉ được đặt trong vòng 7 ngày tới!");
        }

        // Kiểm tra khung giờ
        LocalTime start = startTime.toLocalTime();
        LocalTime end = endTime.toLocalTime();
        boolean isValidTime = switch (shiftType) {
            case "Sáng" -> start.equals(LocalTime.of(10, 30)) && end.equals(LocalTime.of(13, 30));
            case "Chiều" -> start.equals(LocalTime.of(14, 0)) && end.equals(LocalTime.of(17, 0));
            case "Tối" -> start.equals(LocalTime.of(17, 30)) && end.equals(LocalTime.of(20, 30));
            case "Đêm" -> start.equals(LocalTime.of(21, 0)) && end.equals(LocalTime.of(9, 50));
            default -> false;
        };

        if (!isValidTime) {
            System.out.println("❌ LỖI: Ca sai khung giờ quy định!");
            throw new IllegalArgumentException("❌ LỖI: Ca " + shiftType + " sai khung giờ quy định!");
        }
        switch (shiftType.toLowerCase()) {
            case "sáng", "chiều", "tối":
                System.out.println("✅ Trả về giá ca Ngày (Sáng/Chiều/Tối): 150.00");
                return basePrice;
            case "đêm" :
                System.out.println("✅ Trả về giá ca Đêm: 450.00");
                return overPrice;
            default:
                System.out.println("❌ LỖI: Loại ca '" + shiftType + "' không hợp lệ!");
                throw new IllegalArgumentException("❌ LỖI: Loại ca " + shiftType + " không tồn tại!!");
        }
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

    /**
     * Hàm tổng hợp: Vừa kiểm tra khung giờ, vừa tính tổng tiền.
     *
     * @return Tổng số tiền dưới dạng String (VD: "250000")
     * @throws IllegalArgumentException nếu có ca không hợp lệ
     */
    public BigDecimal calculateTotalAmount(List<WorkShift> group, Long roomId, RedirectAttributes redirectAttributes) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phòng ID: " + roomId));
        BigDecimal total = BigDecimal.ZERO;
        // 2. Duyệt từng ca
        for (WorkShift shift : group) {
            // Validate thời gian - Nếu lỗi thì ném Exception để dừng luồng xử lý
            try {
                BigDecimal shiftPrice = validateAndCalculate(
                        shift.getWorkDate(),
                        shift.getStartTime(),
                        shift.getEndTime(),
                        shift.getShiftType(), roomId
                );
                total = total.add(shiftPrice);
                System.out.println(total + " ");
                // Tiếp tục xử lý lưu DB
            } catch (IllegalArgumentException e) {
                String errorMsg = e.getMessage(); // Đây chính là String thông báo lỗi bạn muốn
                redirectAttributes.addFlashAttribute("toastMessage", errorMsg);
            }


        }

        // Chuyển BigDecimal về Long (Kiểu số nguyên 64-bit, an toàn cho tiền tệ)
        return total;
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
