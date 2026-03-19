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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

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

    private static final long MAX_FILE_SIZE = 2 * 1024 * 1024;

    private static final Set<String> ALLOWED_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp"
    );
    
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

    // ========================Hoang Han=============================


    public List<Booking> getBookingByEmail(String email) {
        return bookingRepository.getBookingByCustomer_Email(email).orElse(null);
    }

    public Booking getBookingDetail(String code) {
        return bookingRepository.findByBookingCode(code).orElse(null);
    }

    @Transactional
    public void updateStatus(BookingStatus bookingStatus, String bookingCode) {
        Booking booking = getBookingDetail(bookingCode);
        booking.setBookingStatus(bookingStatus);
    }

    @Transactional
    public void cancel(Long bookingId) {
        Booking booking = getBookingById(bookingId);
        booking.setNote("Được hủy bởi khách hàng");
        booking.setBookingStatus(BookingStatus.FAILED);
    }


    @Transactional
    public void updateIdCard(MultipartFile idCardFront, MultipartFile idCardBack, Long bookingId)
            throws Exception{

        if (idCardFront.getSize() > MAX_FILE_SIZE || idCardBack.getSize() > MAX_FILE_SIZE) {
            throw new Exception("Ảnh phải nhỏ hơn hoặc bằng 2mb");
        }

        if (!ALLOWED_TYPES.contains(idCardFront.getContentType()) || !ALLOWED_TYPES.contains(idCardBack.getContentType())) {
            throw new Exception("Hệ thống chỉ hỗ trợ ảnh có đuôi .png hoặc .jpg");
        }
        Booking booking = bookingRepository.getReferenceById(bookingId);

        try {

            @SuppressWarnings("unchecked")
            Map<String, Object> uploadidCardFrontResult = cloudinary.uploader().upload(
                    idCardFront.getBytes(),
                    ObjectUtils.asMap(
                            "folder", "book-now/card-id"
                    ));

            @SuppressWarnings("unchecked")
            Map<String, Object> uploadIdCardBackResult = cloudinary.uploader().upload(
                    idCardBack.getBytes(),
                    ObjectUtils.asMap(
                            "folder", "book-now/card-id"
                    ));

            String idCardFrontUrl = (String) uploadidCardFrontResult.get("secure_url");
            String publicIdCardFrontUrl = (String) uploadidCardFrontResult.get("public_id");

            String idCardBackUrl = (String) uploadIdCardBackResult.get("secure_url");
            String publicIdCardBackUrl = (String) uploadIdCardBackResult.get("public_id");

            booking.setIdCardFrontUrl(idCardFrontUrl);
            booking.setIdCardFontPublicId(publicIdCardFrontUrl);

            booking.setIdCardBackUrl(idCardBackUrl);
            booking.setIdCardBackPublicId(publicIdCardBackUrl);

            updateStatus(BookingStatus.PENDING, booking.getBookingCode());

        } catch (Exception e) {
            throw new Exception("Lỗi upload ảnh lên cloud");
        }
    }


    @SuppressWarnings("null")
    public Booking getBookingById(Long bookingId) {
        return bookingRepository.findById(bookingId).orElse(null);
    }

    // ========================Hoang Han=============================





    private Bucket createNewBucket() {
        return Bucket.builder()
                .addLimit(Bandwidth.classic(1, Refill.intervally(1, Duration.ofMillis(10))))
                .build();
    }

    public String saveBooking(BookingDTO bookingDTO,
                              MultipartFile frontImg,
                              MultipartFile backImg,
                              RedirectAttributes redirectAttributes,
                              String accessToken, Model model) {

//        String username = jwtService.extractUserName(accessToken);
        String username = "minhanh.nguyen@gmail.com";
        if (isRateLimited(username)) {
            return setErrorMessage(redirectAttributes, "Thao tác quá nhanh! Vui lòng đợi 1 phút.", bookingDTO.getRoomId());
        }
        try {
            // Bước 1: Parse các ca do người dùng CLICK chọn
            List<WorkShift> selectedShifts = processAndValidateShifts(bookingDTO.getSelectedSlots(), bookingDTO.getRoomId());
            System.out.println(" di qua buoc 1");
            // Bước 2: TỰ ĐỘNG LẤP ĐẦY các ca nằm giữa (ví dụ chọn Chiều 13 đến Sáng 14)
            List<WorkShift> allInclusiveShifts = fillMissingShifts(selectedShifts, bookingDTO.getRoomId());
            System.out.println(" di qua buoc 2");
            // Bước 1: Parse và Validate logic cơ bản (Nếu lỗi, ném Exception dừng tại đây)
            List<WorkShift> allShifts = processAndValidateShifts(bookingDTO.getSelectedSlots(), bookingDTO.getRoomId());

            // Bước 2: Kiểm tra trùng lặp trong DB (Nếu đã có người đặt, ném Exception dừng tại đây)
            checkDuplicateShifts(allShifts, bookingDTO.getRoomId());

            // Bước 3: CHỈ KHI QUA ĐƯỢC 2 BƯỚC TRÊN MỚI CHẠY DÒNG NÀY
            saveSingleBookingToDatabase(allInclusiveShifts, bookingDTO, username, redirectAttributes, frontImg, backImg);

            redirectAttributes.addFlashAttribute("toastMessage", "Đặt phòng thành công!");
            redirectAttributes.addFlashAttribute("toastType", "success");
            return "redirect:/payment";

        } catch (IllegalArgumentException e) {
            // Catch mọi lỗi từ validate hoặc check trùng để hiển thị Toast
            return setErrorMessage(redirectAttributes, e.getMessage(), bookingDTO.getRoomId());
        }
    }

    // --- CÁC HÀM ĐÃ TÁCH (Dễ dàng Unit Test) ---

    public List<WorkShift> processAndValidateShifts(List<String> selectedSlots, Long roomId) {
        if (selectedSlots == null || selectedSlots.isEmpty()) {
            throw new IllegalArgumentException("Vui lòng chọn ít nhất một slot!");
        }

        List<WorkShift> shifts = new ArrayList<>();
        for (String slot : selectedSlots) {
            WorkShift shift = parseShift(slot);
            // KHÔNG dùng try-catch ở đây. Nếu validate lỗi, nó sẽ văng Exception ra ngoài và dừng hàm luôn.
            validateAndCalculate(shift.getWorkDate(), shift.getStartTime(), shift.getEndTime(), shift.getShiftType(), roomId);
            shifts.add(shift);
        }

        // Sắp xếp
        Map<String, Integer> shiftOrder = Map.of("Sáng", 0, "Chiều", 1, "Tối", 2, "Đêm", 3);
        shifts.sort((o1, o2) -> {
            int dateCompare = o1.getWorkDate().toLocalDate().compareTo(o2.getWorkDate().toLocalDate());
            return (dateCompare != 0) ? dateCompare : Integer.compare(shiftOrder.get(o1.getShiftType()), shiftOrder.get(o2.getShiftType()));
        });

        return shifts;
    }
    private void saveSingleBookingToDatabase(List<WorkShift> allShifts,
                                             BookingDTO bookingDTO,
                                             String email,
                                             RedirectAttributes redirectAttributes,
                                             MultipartFile frontImg,
                                             MultipartFile backImg) {

        if (allShifts.isEmpty()) return;

        List<Timetable> timetableList = timeTableRepository.findAll();
        Customer customer = customerRepository.getCustomerByEmail(email);

        // Lấy ca đầu và ca cuối để làm mốc Check-in/Check-out cho cả đơn hàng
        WorkShift firstShift = allShifts.get(0);
        WorkShift lastShift = allShifts.get(allShifts.size() - 1);

        // 1. Tính tổng tiền của TẤT CẢ các ca đã chọn
        BigDecimal totalAmount = calculateTotalAmount(allShifts, bookingDTO.getRoomId(), redirectAttributes);

        LocalDateTime checkInDate = firstShift.getStartTime();
        LocalDateTime checkOutDate = lastShift.getEndTime();

        // Nếu ca cuối cùng là ca Đêm, ngày check-out thực tế là sáng hôm sau
        if ("Đêm".equals(lastShift.getShiftType())) {
            // Lưu ý: parseShift của bạn đã cộng 1 ngày cho endDateTime của ca Đêm rồi,
            // nên checkOutDate = lastShift.getEndTime() thường đã là sáng hôm sau.
        }

        // 2. Tạo 1 BOOKING DUY NHẤT
        Booking newBooking = new Booking();
        newBooking.setCustomer(customer);
        Room room = new Room();
        room.setRoomId(bookingDTO.getRoomId());
        newBooking.setRoom(room);

        newBooking.setCheckInTime(checkInDate);
        newBooking.setCheckOutTime(checkOutDate);
        newBooking.setTotalAmount(totalAmount);
        newBooking.setBookingStatus("PENDING");
        newBooking.setBookingCode("BK-" + System.currentTimeMillis());
        newBooking.setCreatedAt(LocalDateTime.now());
        newBooking.setNote(bookingDTO.getNote());

        // Hardcode URL hoặc gọi uploadToCloudinary
        newBooking.setIdCardFrontUrl("https://res.cloudinary.com/dzlfgmtbc/image/upload/v1771992352/zhygftc1kh85gswqjuz8.png");
        newBooking.setIdCardBackUrl("https://res.cloudinary.com/dzlfgmtbc/image/upload/v1771992129/zrvphumlgu2tscz8hcbk.png");
        System.out.println("====================================");
        System.out.println("LOG ĐẶT PHÒNG:");
        System.out.println(" - Ca bắt đầu: " + firstShift.getShiftType());
        System.out.println(" - Ca kết thúc: " + lastShift.getShiftType());
        System.out.println(" - CHECK-IN:  " + checkInDate);
        System.out.println(" - CHECK-OUT: " + checkOutDate);
        System.out.println(" - Tổng tiền: " + calculateTotalAmount(allShifts, bookingDTO.getRoomId(), redirectAttributes));
        System.out.println("====================================");
        Booking savedBooking = bookingRepository.save(newBooking);

        // 3. LƯU CÁC CA VÀO BẢNG SCHEDULER (Chi tiết của đơn hàng)
        for (WorkShift shift : allShifts) {
            Scheduler scheduler = new Scheduler();
            scheduler.setBooking(savedBooking); // Liên kết với đơn hàng vừa tạo
            scheduler.setDate(shift.getWorkDate());

            Long timetableId = null;
            for (Timetable item : timetableList) {
                if (item.getSlotName().contains(shift.getShiftType())) {
                    timetableId = item.getTimetableId();
                    break;
                }
            }

            if (timetableId != null) {
                Timetable tt = new Timetable();
                tt.setTimetableId(timetableId);
                scheduler.setTimetable(tt);
                scheduleRepository.save(scheduler);
            }
        }
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

    // Unit Test
    public void validateAndCalculate(
            LocalDateTime workDatee,
            LocalDateTime startTime, LocalDateTime endTime,
            String shiftType, Long roomId) {
        Room room = roomRepository.getByRoomId(roomId);
        System.out.println("test hàm validate");
        if (room == null) {
            System.out.println("❌ Không tìm thấy phòng ID: " + roomId);
            throw new IllegalArgumentException("Không tìm thấy phòng ID: " + roomId);
        }
        LocalDate today = LocalDate.now();
        LocalDate workDate = workDatee.toLocalDate();
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
        // Giả sử bạn lấy các thông tin cần thiết
        String roomType = room.getRoomType().getName(); // 't' hoặc 'k'
        int slotCount = group.size();
        boolean isVip = false; // Lấy từ Customer nếu có

        long totalVal = 0;
        for (WorkShift shift : group) {
            // Gọi logic tính giá cho từng ca và cộng dồn
            totalVal += calculatePricing(roomType, shift.getShiftType(), slotCount, isVip);
        }

        return BigDecimal.valueOf(totalVal);
    }
    // unit test
    public long calculatePricing(String roomType, String shiftType, int slotCount, boolean isVip) {
        long basePrice = 0;
        if (roomType.equals("Ocean City")) {
            basePrice = 100000;
        } else if (roomType.equals("Mellow")) {
            basePrice = 159000;
        } else { return -1; }
        double shiftMultiplier;
        switch (shiftType) {
            case "Sáng" -> shiftMultiplier = 1.0;
            case "Chiều" -> shiftMultiplier = 1.1;
            case "Tối" -> shiftMultiplier = 1.2;
            case "Đêm" -> shiftMultiplier = 1.5;
            default -> { return -1; }
        }
        double totalBeforeDiscount = basePrice * shiftMultiplier * slotCount;
        double slotDiscountPercent = 1.0;
        if (slotCount >= 4) {
            slotDiscountPercent = 0.07;
        } else if (slotCount > 0) {
            slotDiscountPercent = 0.05;
        } else {
            return -1;
        }
        double vipDiscountPercent = 0.0;
        if (isVip) {
            vipDiscountPercent = 0.10;
        }
        double totalDiscountPercent = slotDiscountPercent + vipDiscountPercent;
        double finalAmount = totalBeforeDiscount * (1 - totalDiscountPercent);
        return Math.round(finalAmount);
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
    private void checkDuplicateShifts(List<WorkShift> allShifts, Long roomId) {
        if (allShifts == null || allShifts.isEmpty()) return;

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        // 1. Kiểm tra trùng lặp nội bộ (Chặn chọn trùng 1 slot trong cùng 1 đơn)
        Set<String> internalCheck = new HashSet<>();
        for (WorkShift shift : allShifts) {
            String uniqueKey = shift.getWorkDate().toLocalDate().toString() + "-" + shift.getShiftType();
            if (!internalCheck.add(uniqueKey)) {
                throw new IllegalArgumentException("Bạn không thể chọn trùng ca " + shift.getShiftType() + " ngày " + shift.getWorkDate().format(dateFormatter));
            }
        }

        // 2. Lấy mốc thời gian bao quát toàn bộ các ca đã chọn
        // Vì danh sách allShifts đã được sắp xếp theo thời gian trong hàm processAndValidateShifts
        LocalDateTime overallStart = allShifts.get(0).getStartTime();
        LocalDateTime overallEnd = allShifts.get(allShifts.size() - 1).getEndTime();

        System.out.println("--- KIỂM TRA TRÙNG LẶP TỔNG THỂ ---");
        System.out.println("Room ID: " + roomId);
        System.out.println("Khoảng thời gian cần check: " + overallStart.format(timeFormatter) + " -> " + overallEnd.format(timeFormatter));

        // 3. Chỉ gọi Repo 1 lần duy nhất với khoảng thời gian lớn nhất
        boolean isOccupied = bookingRepository.isRoomOccupied(
                roomId,
                overallStart,
                overallEnd
        );

        if (isOccupied) {
            // Tìm ca cụ thể bị vướng (Tùy chọn: Có thể in thông báo chung hoặc duyệt lại để báo ca cụ thể)
            throw new IllegalArgumentException(String.format(
                    "Rất tiếc, phòng đã có người đặt trong khoảng thời gian từ %s ngày %s đến %s ngày %s.",
                    overallStart.toLocalTime(), overallStart.format(dateFormatter),
                    overallEnd.toLocalTime(), overallEnd.format(dateFormatter)
            ));
        }
    }
    private List<WorkShift> fillMissingShifts(List<WorkShift> selectedShifts, Long roomId) {
        if (selectedShifts.size() < 2) return selectedShifts;

        List<WorkShift> fullList = new ArrayList<>();
        List<Timetable> timetableList = timeTableRepository.findAll();
        // Sắp xếp timetable theo thứ tự Sáng -> Chiều -> Tối -> Đêm
        timetableList.sort(Comparator.comparing(Timetable::getTimetableId));

        WorkShift first = selectedShifts.get(0);
        WorkShift last = selectedShifts.get(selectedShifts.size() - 1);

        LocalDateTime currentPointer = first.getStartTime();
        LocalDateTime endPointer = last.getStartTime();

        while (!currentPointer.isAfter(endPointer)) {
            for (Timetable slot : timetableList) {
                LocalDateTime slotStart = LocalDateTime.of(currentPointer.toLocalDate(), slot.getStartTime());
                LocalDateTime slotEnd = LocalDateTime.of(currentPointer.toLocalDate(), slot.getEndTime());

                // Xử lý ca đêm qua ngày hôm sau
                if (slot.getEndTime().isBefore(slot.getStartTime())) {
                    slotEnd = slotEnd.plusDays(1);
                }

                // Nếu ca này nằm trong khoảng từ ca đầu đến ca cuối khách chọn
                if (!slotStart.isBefore(first.getStartTime()) && !slotStart.isAfter(last.getStartTime())) {
                    String type = extractType(slot.getSlotName());
                    fullList.add(new WorkShift(slotStart.toLocalDate().atStartOfDay(), slotStart, slotEnd, type));
                }
            }
            currentPointer = currentPointer.plusDays(1);
        }
        return fullList;
    }

    private String extractType(String slotName) {
        if (slotName.contains("Sáng")) return "Sáng";
        if (slotName.contains("Chiều")) return "Chiều";
        if (slotName.contains("Tối")) return "Tối";
        return "Đêm";
    }

}
