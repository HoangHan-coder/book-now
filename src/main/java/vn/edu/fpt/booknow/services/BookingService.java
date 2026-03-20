package vn.edu.fpt.booknow.services;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.transaction.Transactional;
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
                .addLimit(Bandwidth.classic(1, Refill.intervally(1, Duration.ofMinutes(1))))
                .build();
    }

    public String saveBooking(BookingDTO bookingDTO,
                              MultipartFile frontImg,
                              MultipartFile backImg,
                              RedirectAttributes redirectAttributes,
                              String accessToken) {
        String username = jwtService.extractUserName(accessToken);

//        String username = "minhanh.nguyen@gmail.com";


        try {
            if (isRateLimited(username)) {
                return setErrorMessage(redirectAttributes, "Thao tác quá nhanh! Vui lòng đợi 1 phút.", bookingDTO.getRoom().getRoomId());
            }
            if (frontImg.getSize() == 0) {
                return setErrorMessage(redirectAttributes, "Lỗi upload ảnh mặt trước!", bookingDTO.getRoom().getRoomId());

            }
            if (backImg.getSize() == 0) {
                return setErrorMessage(redirectAttributes, "Lỗi upload ảnh mặt sau!", bookingDTO.getRoom().getRoomId());

            }
            // 1. Parse và tự động gán năm cho Check-in/Check-out (Xử lý loại 1 & 2)
            WorkShift firstShift = parseToWorkShift(bookingDTO.getCheckInTime());
            WorkShift lastShift = parseToWorkShift(bookingDTO.getCheckOutTime());
            if (lastShift.getStartTime().isBefore(firstShift.getStartTime())) {
                return setErrorMessage(redirectAttributes, "Ngày trả phòng không thể trước ngày nhận phòng!", bookingDTO.getRoom().getRoomId());
            }
            // 2. Kiểm tra khung giờ so với DB (Yêu cầu 3)
            String result = validateShiftWithDatabase(bookingDTO.getRoom().getRoomId(), firstShift, lastShift);
            if (!result.isEmpty()) {
                return setErrorMessage(redirectAttributes, result, bookingDTO.getRoom().getRoomId());
            }
            // 3. Lấp đầy các ca ở giữa để kiểm tra tính liên tiếp (Yêu cầu 4)
            List<WorkShift> allShifts = fillMissingShifts(Arrays.asList(firstShift, lastShift), bookingDTO.getRoom().getRoomId());

            // 4. Kiểm tra trùng lặp trong DB
            String resultCheck = checkDuplicateShifts(allShifts, bookingDTO.getRoom().getRoomId());
            if (!resultCheck.isEmpty()) {
                return setErrorMessage(redirectAttributes, resultCheck, bookingDTO.getRoom().getRoomId());
            }
            // 5. Upload ảnh (Giữ lại logic cũ)
            String frontUrl = "";
            if (frontImg != null && !frontImg.isEmpty()) {
                try {
                    frontUrl = uploadToCloudinary(frontImg);
                } catch (IOException e) {
                    return setErrorMessage(redirectAttributes, "Lỗi upload ảnh mặt trước!", bookingDTO.getRoom().getRoomId());
                }
            }
            String backUrl = "";
            if (backImg != null && !backImg.isEmpty()) {
                try {
                    backUrl = uploadToCloudinary(backImg);
                } catch (IOException e) {
                    return setErrorMessage(redirectAttributes, "Lỗi upload ảnh mặt sau!", bookingDTO.getRoom().getRoomId());
                }
            }
            // 6. Lưu vào Database
            saveSingleBookingToDatabase(allShifts, bookingDTO, username, redirectAttributes, frontUrl, backUrl);

            redirectAttributes.addFlashAttribute("toastMessage", "Đặt phòng thành công!");
            redirectAttributes.addFlashAttribute("toastType", "success");
            return "redirect:/payment";

        } catch (IllegalArgumentException e) {
            return setErrorMessage(redirectAttributes, e.getMessage(), bookingDTO.getRoom().getRoomId());
        }
    }

    /**
     * Parse String từ DTO và tự động gán năm nếu thiếu
     */
    private WorkShift parseToWorkShift(String input) {
        try {
            if (input == null || input.trim().isEmpty()) {
                throw new IllegalArgumentException("Dữ liệu thời gian không được để trống!");
            }

            // 1. Tách phần ngày: "25/03", "25" hoặc "25/03/2026"
            String[] inputParts = input.split(" ");
            String datePart = inputParts[0].trim();

            LocalDate now = LocalDate.now();
            int day, month, year;

            // 2. Xử lý logic parse ngày (Bắt lỗi "For input string" tại đây)
            try {
                if (datePart.contains("/")) {
                    String[] parts = datePart.split("/");
                    day = Integer.parseInt(parts[0].trim());
                    month = Integer.parseInt(parts[1].trim());
                    year = (parts.length == 3) ? Integer.parseInt(parts[2].trim()) : now.getYear();
                } else {
                    // Trường hợp chỉ có ngày "25" -> Tự lấy tháng/năm hiện tại
                    day = Integer.parseInt(datePart);
                    month = now.getMonthValue();
                    year = now.getYear();
                }
            } catch (NumberFormatException e) {
                // Thay vì lỗi hệ thống, trả về thông báo Toast thân thiện
                throw new IllegalArgumentException("Ngày '" + datePart + "' không đúng định dạng số!");
            }

            LocalDate baseDate = LocalDate.of(year, month, day);

            // 3. Kiểm tra ngày trong quá khứ
            if (baseDate.isBefore(now)) {
                throw new IllegalArgumentException("Ngày " + datePart + " đã trôi qua, vui lòng chọn ngày khác!");
            }

            // 4. Parse giờ (VD: (10:30 - 13:30))
            if (!input.contains("(") || !input.contains(")")) {
                throw new IllegalArgumentException("Thiếu khung giờ cụ thể!");
            }

            String timePart = input.substring(input.indexOf("(") + 1, input.indexOf(")")).replace("h", ":");
            String[] times = timePart.split(" - ");

            LocalTime startT = LocalTime.parse(times[0].trim());
            LocalTime endT = LocalTime.parse(times[1].trim());

            LocalDateTime startDateTime = baseDate.atTime(startT);
            LocalDateTime endDateTime = baseDate.atTime(endT);

            if (endT.isBefore(startT)) endDateTime = endDateTime.plusDays(1);

            return new WorkShift(baseDate.atStartOfDay(), startDateTime, endDateTime, extractType(input));

        } catch (IllegalArgumentException e) {
            // Ném lỗi logic để hàm saveBooking bắt được
            throw e;
        } catch (Exception e) {
            // Bắt mọi lỗi định dạng khác (lỗi split, lỗi parse giờ...)
            throw new IllegalArgumentException("Định dạng '" + input + "' không hợp lệ!");
        }
    }

    /**
     * Kiểm tra khung giờ khớp với Timetable trong DB
     */
    private String validateShiftWithDatabase(Long roomId, WorkShift... shifts) {
        List<Timetable> timetableList = timeTableRepository.findAll();

        for (WorkShift s : shifts) {
            boolean isValid = timetableList.stream().anyMatch(t ->
                    t.getSlotName().contains(s.getShiftType()) &&
                            t.getStartTime().equals(s.getStartTime().toLocalTime())
            );
            if (!isValid) return "Ca " + s.getShiftType() + " không đúng khung giờ quy định của hệ thống!";
        }
        return ""; // Không có lỗi
    }
    private void saveSingleBookingToDatabase(List<WorkShift> allShifts,
                                             BookingDTO bookingDTO,
                                             String email,
                                             RedirectAttributes redirectAttributes,
                                             String frontImg,
                                             String backImg) {

           if (allShifts.isEmpty()) return;

           List<Timetable> timetableList = timeTableRepository.findAll();
           Customer customer = customerRepository.getCustomerByEmail(email);

           // Lấy ca đầu và ca cuối để làm mốc Check-in/Check-out cho cả đơn hàng
           WorkShift firstShift = allShifts.get(0);
           WorkShift lastShift = allShifts.get(allShifts.size() - 1);

           // 1. Tính tổng tiền của TẤT CẢ các ca đã chọn
           BigDecimal totalAmount = calculateTotalAmount(allShifts, bookingDTO.getRoom().getRoomId());

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
           room.setRoomId(bookingDTO.getRoom().getRoomId());
           newBooking.setRoom(room);

           newBooking.setCheckInTime(checkInDate);
           newBooking.setCheckOutTime(checkOutDate);
           newBooking.setTotalAmount(totalAmount);
           newBooking.setBookingStatus(BookingStatus.PENDING_PAYMENT);
           newBooking.setBookingCode(generateUniqueBookingCode());
           newBooking.setCreatedAt(LocalDateTime.now());
           newBooking.setNote(bookingDTO.getNote());

           // Hardcode URL hoặc gọi uploadToCloudinary
           newBooking.setIdCardFrontUrl(frontImg);
           newBooking.setIdCardBackUrl(backImg);
           System.out.println("====================================");
           System.out.println("LOG ĐẶT PHÒNG:");
           System.out.println(" - Ca bắt đầu: " + firstShift.getShiftType());
           System.out.println(" - Ca kết thúc: " + lastShift.getShiftType());
           System.out.println(" - CHECK-IN:  " + checkInDate);
           System.out.println(" - CHECK-OUT: " + checkOutDate);
           System.out.println(" - Tổng tiền: " + calculateTotalAmount(allShifts, bookingDTO.getRoom().getRoomId()));
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
    /**
     * Tính tổng tiền của đơn hàng dựa trên giá phòng từ Database.
     * Sáng, Chiều, Tối = basePrice
     * Đêm = overnightPrice
     */
    public BigDecimal calculateTotalAmount(List<WorkShift> group, Long roomId) {

        // 2. Lấy giá cấu hình của phòng
        Room room1 = roomRepository.getPrice(roomId);
        BigDecimal dayPrice = room1.getRoomType().getBasePrice();      // Giá cho ca Sáng/Chiều/Tối
        BigDecimal nightPrice = room1.getRoomType().getOverPrice(); // Giá cho ca Đêm

        BigDecimal total = BigDecimal.ZERO;

        // 3. Duyệt qua danh sách ca đã được validate và fill đầy đủ
        for (WorkShift shift : group) {
            if ("Đêm".equals(shift.getShiftType())) {
                total = total.add(nightPrice);
            } else {
                total = total.add(dayPrice);
            }
        }

        return total;
    }
    public String generateUniqueBookingCode() {
        String newCode;
        Booking isExisted = new Booking();

        do {
            // Tạo mã theo format BK + Timestamp hiện tại
            // Ví dụ: BK-1710912345678
            newCode = "BK-" + System.currentTimeMillis();

            // Kiểm tra mã này đã tồn tại trong DB chưa
            isExisted = bookingRepository.getByBookingCode(newCode);

            // Nếu đã tồn tại (isExisted = true), vòng lặp sẽ tiếp tục chạy lại
        } while (isExisted != null);

        return newCode;
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


    /**
     * Hàm tổng hợp: Vừa kiểm tra khung giờ, vừa tính tổng tiền.
     *
     * @return Tổng số tiền dưới dạng String (VD: "250000")
     * @throws IllegalArgumentException nếu có ca không hợp lệ
     */
    private String uploadToCloudinary(MultipartFile file) throws IOException {
        // Không try-catch ở đây, để nó tự văng lỗi lên trên
        Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());
        return uploadResult.get("url").toString();
    }
    private String checkDuplicateShifts(List<WorkShift> allShifts, Long roomId) {
        if (allShifts == null || allShifts.isEmpty()) return "";

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        // 1. Kiểm tra trùng lặp nội bộ (Chặn chọn trùng 1 slot trong cùng 1 đơn)
        Set<String> internalCheck = new HashSet<>();
        for (WorkShift shift : allShifts) {
            String uniqueKey = shift.getWorkDate().toLocalDate().toString() + "-" + shift.getShiftType();
            if (!internalCheck.add(uniqueKey)) {
                System.out.println("tesst 286");
                return "Bạn chọn trùng ca " + shift.getShiftType() + " ngày " + shift.getWorkDate().format(dateFormatter);
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
            return "Rất tiếc, phòng đã có người đặt trong khoảng thời gian này.";
        }
        return "";
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
    public Booking getFindCode(String code) {
        Booking booking = bookingRepository.getByBookingCode(code);
        return booking;
    }
    @Transactional
    public String completeOfflineCheckin(Booking bookingData, MultipartFile frontImg, MultipartFile backImg, RedirectAttributes redirectAttributes) {
        // 1. Lấy dữ liệu gốc từ DB
        Booking existingBooking = bookingRepository.findById(bookingData.getBookingId())
                .orElseThrow(() -> new RuntimeException("Booking không tồn tại"));

        // 2. Cập nhật các trường thông tin cơ bản
        existingBooking.setNote(bookingData.getNote());
        existingBooking.setBookingStatus(BookingStatus.CHECKED_IN);
        existingBooking.setUpdateAt(LocalDateTime.now());

        // 3. Xử lý ảnh mặt trước
        if (frontImg != null && !frontImg.isEmpty()) {
            try {
                String frontUrl = uploadToCloudinary(frontImg);
                // CHỈ set khi upload thành công file mới
                existingBooking.setIdCardFrontUrl(frontUrl);
            } catch (IOException e) {
                return setErrorMessage(redirectAttributes, "Lỗi upload ảnh mặt trước!", bookingData.getBookingId());
            }
        }
        // Nếu không có frontImg mới, existingBooking.getIdCardFrontUrl() vẫn giữ giá trị cũ từ DB

        // 4. Xử lý ảnh mặt sau
        if (backImg != null && !backImg.isEmpty()) {
            try {
                String backUrl = uploadToCloudinary(backImg);
                // CHỈ set khi upload thành công file mới
                existingBooking.setIdCardBackUrl(backUrl);
            } catch (IOException e) {
                return setErrorMessage(redirectAttributes, "Lỗi upload ảnh mặt sau!", bookingData.getBookingId());
            }
        }

        // 5. Lưu lại Booking đã cập nhật
        bookingRepository.save(existingBooking);

        redirectAttributes.addFlashAttribute("toastMessage", "Check-in thành công!");
        redirectAttributes.addFlashAttribute("toastType", "success");

        return "redirect:/offline-checkin";
    }
    @Transactional
    public void cancelBookingStatus(Long bookingId) {
        // 1. Tìm booking
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn đặt phòng để hủy"));

        // 2. Kiểm tra điều kiện (Ví dụ: Không được hủy nếu đã Check-in)
        if ("CHECKED_IN".equals(booking.getBookingStatus())) {
            throw new RuntimeException("Không thể hủy đơn đã hoàn tất Check-in!");
        }
        System.out.println(bookingId + " test 494");
        // 3. Cập nhật trạng thái
        booking.setBookingStatus(BookingStatus.FAILED);
        booking.setUpdateAt(LocalDateTime.now());

        // 4. Lưu lại
        bookingRepository.save(booking);
    }
}
