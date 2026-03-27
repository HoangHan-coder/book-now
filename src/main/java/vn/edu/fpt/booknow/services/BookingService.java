package vn.edu.fpt.booknow.services;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.edu.fpt.booknow.model.dto.BookingCustomerDTO;
import vn.edu.fpt.booknow.model.dto.WorkShift;
import vn.edu.fpt.booknow.model.entities.*;
import vn.edu.fpt.booknow.repositories.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.security.SecureRandom;
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

    @Autowired
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


    public Booking findById(long id) {
        return bookingRepository.findById(id).orElse(null);
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

            updateStatus(BookingStatus.PAID, booking.getBookingCode());

        } catch (Exception e) {
            throw new Exception("Lỗi upload ảnh lên cloud");
        }
    }


    @SuppressWarnings("null")
    public Booking getBookingById(Long bookingId) {
        return bookingRepository.findById(bookingId).orElse(null);
    }

    // ========================Hoang Han=============================




    // ========================Tan Loc=============================
    private Bucket createNewBucket() {
        return Bucket.builder()
                .addLimit(Bandwidth.classic(1, Refill.intervally(1, Duration.ofMinutes(1))))
                .build();
    }

    public Booking save(Booking booking) {
        return bookingRepository.save(booking);
    }

    public String saveBooking(BookingCustomerDTO bookingDTO,
                              MultipartFile frontImg,
                              MultipartFile backImg,
                              RedirectAttributes redirectAttributes,
                              String accessToken) {


        try {
            String username = jwtService.extractUserName(accessToken);
            if (isRateLimited(username)) {
                return setErrorMessage(redirectAttributes, "Thao tác quá nhanh! Vui lòng đợi 1 phút.", bookingDTO.getRoom().getRoomId());
            }
            if (frontImg.getSize() == 0) {
                return setErrorMessage(redirectAttributes, "Lỗi upload ảnh mặt trước!", bookingDTO.getRoom().getRoomId());

            }
            if (backImg.getSize() == 0) {
                return setErrorMessage(redirectAttributes, "Lỗi upload ảnh mặt sau!", bookingDTO.getRoom().getRoomId());

            }
            WorkShift firstShift = parseToWorkShift(bookingDTO.getCheckInTime());
            WorkShift lastShift = parseToWorkShift(bookingDTO.getCheckOutTime());
            if (lastShift.getStartTime().isBefore(firstShift.getStartTime())) {
                return setErrorMessage(redirectAttributes, "Ngày trả phòng không thể trước ngày nhận phòng!", bookingDTO.getRoom().getRoomId());
            }
            String result = validateShiftWithDatabase(bookingDTO.getRoom().getRoomId(), firstShift, lastShift);
            if (!result.isEmpty()) {
                return setErrorMessage(redirectAttributes, result, bookingDTO.getRoom().getRoomId());
            }
            List<WorkShift> allShifts = fillMissingShifts(Arrays.asList(firstShift, lastShift));
            if (allShifts == null) {
                return setErrorMessage(redirectAttributes, "Thời gian không hợp lệ!", bookingDTO.getRoom().getRoomId());
            }
            String resultCheck = checkDuplicateShifts(allShifts, bookingDTO.getRoom().getRoomId());
            if (!resultCheck.isEmpty()) {
                return setErrorMessage(redirectAttributes, resultCheck, bookingDTO.getRoom().getRoomId());
            }
            String frontUrl = "";
            String frontId = "";
            if (frontImg != null && !frontImg.isEmpty()) {
                try {
                    Map<String, String> imageData = uploadToCloudinary(frontImg);
                    frontUrl = imageData.get("url");
                    frontId = imageData.get("public_id");
                } catch (IOException e) {
                    return setErrorMessage(redirectAttributes, "Lỗi upload ảnh mặt trước!", bookingDTO.getRoom().getRoomId());
                }
            }
            String backUrl = "";
            String backId = "";
            if (backImg != null && !backImg.isEmpty()) {
                try {
                    Map<String, String> imageData = uploadToCloudinary(frontImg);
                    backUrl = imageData.get("url");
                    backId = imageData.get("public_id");
                } catch (IOException e) {
                    return setErrorMessage(redirectAttributes, "Lỗi upload ảnh mặt sau!", bookingDTO.getRoom().getRoomId());
                }
            }
            // 6. Lưu vào Database
            Booking booking = saveSingleBookingToDatabase(allShifts, bookingDTO, username, redirectAttributes, frontUrl, backUrl, frontId, backId);

//            redirectAttributes.addFlashAttribute("toastMessage", "Đặt phòng thành công!");
//            redirectAttributes.addFlashAttribute("toastType", "success");
            if (booking == null) {
                return setErrorMessage(redirectAttributes,  "Đặt phòng không thành công!", bookingDTO.getRoom().getRoomId());
            }
            return "redirect:/bookings/" + booking.getBookingId();

        } catch (Exception e) {
            return setErrorMessage(redirectAttributes, e.getMessage(), bookingDTO.getRoom().getRoomId());
        }
    }

    private WorkShift parseToWorkShift(String input) {
        try {
            if (input == null || input.trim().isEmpty()) {
                throw new IllegalArgumentException("Dữ liệu thời gian không được để trống!");
            }

            // "25/03", "25" And "25/03/2026"
            String[] inputParts = input.split(" ");
            String datePart = inputParts[0].trim();

            LocalDate now = LocalDate.now();
            int day, month, year;

            // Error String input
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
                throw new IllegalArgumentException("Ngày '" + datePart + "' không đúng định dạng số!");
            }

            LocalDate baseDate = LocalDate.of(year, month, day);

            // Check past Day
            if (baseDate.isBefore(now)) {
                throw new IllegalArgumentException("Ngày " + datePart + " đã trôi qua, vui lòng chọn ngày khác!");
            }

            // 4. Parse Hour (VD: (10:30 - 13:30))
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
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("Định dạng '" + input + "' không hợp lệ!");
        }
    }

    @Transactional
    public String completeOfflineCheckin(Booking bookingData, MultipartFile frontImg, MultipartFile backImg, RedirectAttributes redirectAttributes) {
        try {
            Booking existingBooking = bookingRepository.findById(bookingData.getBookingId())
                    .orElseThrow(() -> new RuntimeException("Booking không tồn tại"));
            if (isRateLimited(bookingData.getBookingCode())) {
                redirectAttributes.addFlashAttribute("toastMessage", "Thao tác quá nhanh vui lòng tải lại trang!!!");
                redirectAttributes.addFlashAttribute("toastType", "error");
                return "redirect:/staff/offline-checkin?searchTerm=" + bookingData.getBookingCode();
            }
            if (frontImg != null && !frontImg.isEmpty()) {
                try {
                    Map<String, String> imageData = uploadToCloudinary(frontImg);
                    existingBooking.setIdCardFrontUrl(imageData.get("url"));
                    existingBooking.setIdCardFontPublicId(imageData.get("public_id"));
                } catch (IOException e) {
                    return setErrorMessage(redirectAttributes, "Lỗi upload ảnh mặt trước!", bookingData.getBookingId());
                }
            }
            if (backImg != null && !backImg.isEmpty()) {
                try {
                    Map<String, String> imageData = uploadToCloudinary(backImg);
                    existingBooking.setIdCardBackUrl(imageData.get("url"));
                    existingBooking.setIdCardBackPublicId(imageData.get("public_id"));
                } catch (IOException e) {
                    return setErrorMessage(redirectAttributes, "Lỗi upload ảnh mặt sau!", bookingData.getBookingId());
                }
            }
            if (BookingStatus.REJECTED_CHECKIN.equals(existingBooking.getBookingStatus()) || BookingStatus.APPROVED.equals(existingBooking.getBookingStatus())) {
                existingBooking.setNote(bookingData.getNote());
                existingBooking.setBookingStatus(BookingStatus.CHECKED_IN);
                existingBooking.setUpdatedAt(LocalDateTime.now());
                bookingRepository.save(existingBooking);
                redirectAttributes.addFlashAttribute("toastMessage", "Check-in thành công!");
                redirectAttributes.addFlashAttribute("toastType", "success");
                return "redirect:/staff/offline-checkin?searchTerm=" + bookingData.getBookingCode();
            }
            redirectAttributes.addFlashAttribute("toastMessage", "Lỗi đơn đã hủy!");
            redirectAttributes.addFlashAttribute("toastType", "error");
            return "redirect:/staff/offline-checkin?searchTerm=" + bookingData.getBookingCode();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("toastMessage", e.getMessage());
            redirectAttributes.addFlashAttribute("toastType", "error");
            return "redirect:/staff/offline-checkin?searchTerm=" + bookingData.getBookingCode();
        }
    }

    @Transactional
    public void cancelBookingStatus(Long bookingId, RedirectAttributes redirectAttributes) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn đặt phòng để hủy"));

        if (BookingStatus.CHECKED_IN.equals(booking.getBookingStatus())) {
            redirectAttributes.addFlashAttribute("toastMessage", "Lỗi đơn đã checkin!");
            redirectAttributes.addFlashAttribute("toastType", "error");
            return;
        }
        System.out.println(bookingId + " test 494");
        booking.setBookingStatus(BookingStatus.CANCELED);
        booking.setUpdatedAt(LocalDateTime.now());

        bookingRepository.save(booking);
    }

    private String validateShiftWithDatabase(Long roomId, WorkShift... shifts) {
        List<Timetable> timetableList = timeTableRepository.findAll();

        for (WorkShift s : shifts) {
            System.out.println(s.getShiftType() + " 314");
            boolean isValid = timetableList.stream().anyMatch(t ->
                    t.getSlotName().contains(s.getShiftType()) &&
                            t.getStartTime().equals(s.getStartTime().toLocalTime())
            );
            if (!isValid) return "Ca " + s.getShiftType() + " không đúng khung giờ quy định của hệ thống!";
        }
        return "";
    }
    private Booking saveSingleBookingToDatabase(List<WorkShift> allShifts,
                                             BookingCustomerDTO bookingDTO,
                                             String email,
                                             RedirectAttributes redirectAttributes,
                                             String frontImg,
                                             String backImg,
                                             String frontId,
                                             String backId) {

           if (allShifts.isEmpty()) return null;

           List<Timetable> timetableList = timeTableRepository.findAll();
           Customer customer = customerRepository.getCustomerByEmail(email);

           // Use the first and last shifts as the check-in/check-out benchmarks for the entire order.
           WorkShift firstShift = allShifts.get(0);
           WorkShift lastShift = allShifts.get(allShifts.size() - 1);

           BigDecimal totalAmount = calculateTotalAmount(allShifts, bookingDTO.getRoom().getRoomId());

           LocalDateTime checkInDate = firstShift.getStartTime();
           LocalDateTime checkOutDate = lastShift.getEndTime();

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

           newBooking.setIdCardFrontUrl(frontImg);
           newBooking.setIdCardBackUrl(backImg);
           newBooking.setIdCardFontPublicId(frontId);
           newBooking.setIdCardBackPublicId(backId);
           System.out.println("====================================");
           System.out.println("LOG ĐẶT PHÒNG:");
           System.out.println(" - Ca bắt đầu: " + firstShift.getShiftType());
           System.out.println(" - Ca kết thúc: " + lastShift.getShiftType());
           System.out.println(" - CHECK-IN:  " + checkInDate);
           System.out.println(" - CHECK-OUT: " + checkOutDate);
           System.out.println(" - Tổng tiền: " + calculateTotalAmount(allShifts, bookingDTO.getRoom().getRoomId()));
           System.out.println("====================================");
           Booking savedBooking = bookingRepository.save(newBooking);

           for (WorkShift shift : allShifts) {
               Scheduler scheduler = new Scheduler();
               scheduler.setBooking(savedBooking);
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
        return savedBooking;
    }
    public BigDecimal calculateTotalAmount(List<WorkShift> group, Long roomId) {

        Room room1 = roomRepository.getPrice(roomId);
        BigDecimal dayPrice = room1.getRoomType().getBasePrice();      // Giá cho ca Sáng/Chiều/Tối
        BigDecimal nightPrice = room1.getRoomType().getOverPrice(); // Giá cho ca Đêm
        BigDecimal total = BigDecimal.ZERO;
        for (WorkShift shift : group) {
            System.out.println(shift.getShiftType() + " 386");
            if (shift.getShiftType().toLowerCase().contains("đêm")) {
                System.out.println(shift.getShiftType() + " 388");
                total = total.add(nightPrice);
            } else {
                total = total.add(dayPrice);
            }
        }

        return total;
    }
    public String generateUniqueBookingCode() {
        SecureRandom random = new SecureRandom();
        String newCode;

        do {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 13; i++) {
                sb.append(random.nextInt(10));
            }

            newCode = "BK" + sb.toString();

        } while (bookingRepository.getByBookingCode(newCode) != null);

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

    private Map<String, String> uploadToCloudinary(MultipartFile file) throws IOException {
        Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());
        Map<String, String> result = new HashMap<>();
        result.put("url", uploadResult.get("url").toString());
        result.put("public_id", uploadResult.get("public_id").toString());

        return result;
    }
    private String checkDuplicateShifts(List<WorkShift> allShifts, Long roomId) {
        if (allShifts == null || allShifts.isEmpty()) return "";

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        Set<String> internalCheck = new HashSet<>();
        for (WorkShift shift : allShifts) {
            String uniqueKey = shift.getWorkDate().toLocalDate().toString() + "-" + shift.getShiftType();
            if (!internalCheck.add(uniqueKey)) {
                return "Bạn chọn trùng ca " + shift.getShiftType() + " ngày " + shift.getWorkDate().format(dateFormatter);
            }
        }
        LocalDateTime overallStart = allShifts.get(0).getStartTime();
        LocalDateTime overallEnd = allShifts.get(allShifts.size() - 1).getEndTime();

        System.out.println("--- KIỂM TRA TRÙNG LẶP TỔNG THỂ ---");
        System.out.println("Room ID: " + roomId);
        System.out.println("Khoảng thời gian cần check: " + overallStart.format(timeFormatter) + " -> " + overallEnd.format(timeFormatter));
        boolean isOccupied = bookingRepository.isRoomOccupied(
                roomId,
                overallStart,
                overallEnd
        );

        if (isOccupied) {
            return "Rất tiếc, phòng đã có người đặt trong khoảng thời gian này.";
        }
        return "";
    }
    private List<WorkShift> fillMissingShifts(List<WorkShift> selectedShifts) {
        if (selectedShifts.size() < 2) return selectedShifts;

        List<WorkShift> fullList = new ArrayList<>();
        List<Timetable> timetableList = timeTableRepository.findAll();
        timetableList.sort(Comparator.comparing(Timetable::getTimetableId));

        WorkShift first = selectedShifts.get(0);
        WorkShift last = selectedShifts.get(selectedShifts.size() - 1);

        LocalDateTime currentPointer = first.getStartTime();
        LocalDateTime endPointer = last.getStartTime();
        while (!currentPointer.isAfter(endPointer)) {
            for (Timetable slot : timetableList) {
                LocalDateTime slotStart = LocalDateTime.of(currentPointer.toLocalDate(), slot.getStartTime());
                LocalDateTime slotEnd = LocalDateTime.of(currentPointer.toLocalDate(), slot.getEndTime());

                // Processing the night shift the following day
                if (slot.getEndTime().isBefore(slot.getStartTime())) {
                    slotEnd = slotEnd.plusDays(1);
                }

                // If this shift falls within the range of the first and last shifts the customer chooses
                if (!slotStart.isBefore(first.getStartTime()) && !slotStart.isAfter(last.getStartTime())) {
                    String type = extractType(slot.getSlotName());
                    if (type.equalsIgnoreCase("null")) {
                        return null;
                    }
                    fullList.add(new WorkShift(slotStart.toLocalDate().atStartOfDay(), slotStart, slotEnd, type));
                }
            }
            currentPointer = currentPointer.plusDays(1);
        }
        return fullList;
    }

    private String extractType(String slotName) {
        if (slotName == null || slotName.isEmpty()) return "null";

        List<Timetable> allTypes = timeTableRepository.findAll();
        return allTypes.stream()
                .map(Timetable::getSlotName)
                .filter(typeName -> slotName.contains(typeName))
                .findFirst()
                .orElse(null);
    }
    public Booking getFindCode(String code) {
        Booking booking = bookingRepository.getByBookingCode(code);
        return booking;
    }

    @Transactional
    @Scheduled(fixedRate = 60000)
    public void cancelExpiredPendingPayments() {
        LocalDateTime timeLimit = LocalDateTime.now().minusMinutes(1);
        List<Booking> bookings = bookingRepository.findExpiredPendingBookings(timeLimit);
        for (Booking b : bookings) {
            try {
                b.setBookingStatus(BookingStatus.FAILED);
                b.setNote("Hệ thống tự động hủy do quá hạn thanh toán 1 phút.");
                b.setUpdatedAt(LocalDateTime.now());

                bookingRepository.save(b);
                System.out.println("Đã tự động hủy đơn hàng quá hạn: " + b.getBookingId());
            } catch (Exception e) {
                System.err.println("Lỗi khi hủy booking " + b.getBookingId());
            }
        }
    }
}



// ========================Tan Loc=============================
