package vn.edu.fpt.booknow.services;

import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import com.itextpdf.layout.element.Table;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import vn.edu.fpt.booknow.controllers.model.dto.DashboardDTO;
import vn.edu.fpt.booknow.controllers.model.entities.*;
import vn.edu.fpt.booknow.repositories.*;

import java.io.PrintWriter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class ManageRoomServices {

    private RoomRepository roomRepository;
    private RoomAmenityRepository roomAmenityRepository;
    private RoomTypeRepository roomTypeRepository;
    private AmenityRepository amenityRepository;
    private ImageRepository imageRepository;
    private BookingRepository bookingRepository;
    private Cloudinary cloudinary;

    private static final long MAX_FILE_SIZE = 2 * 1024 * 1024;

    private static final java.util.Set<String> ALLOWED_TYPES = java.util.Set.of(
            "image/jpeg",
            "image/png",
            "image/webp"
    );

    public ManageRoomServices(RoomRepository roomRepository, RoomAmenityRepository roomAmenityRepository, RoomTypeRepository roomTypeRepository, AmenityRepository amenityRepository, ImageRepository imageRepository, BookingRepository bookingRepository, Cloudinary cloudinary) {
        this.roomRepository = roomRepository;
        this.roomAmenityRepository = roomAmenityRepository;
        this.roomTypeRepository = roomTypeRepository;
        this.amenityRepository = amenityRepository;
        this.imageRepository = imageRepository;
        this.bookingRepository = bookingRepository;
        this.cloudinary = cloudinary;
    }

    /* ======================================================
                       FILTER ROOM
    ====================================================== */

    @Transactional
    public Page<Room> filterRooms(
            RoomStatus status,
            Long type,
            String roomNumber,
            Pageable pageable
    ) {

        Specification<Room> spec = (root, query, cb) -> cb.conjunction();

        if (status != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("status"), status));
        }

        if (type != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("roomType").get("roomTypeId"), type));
        }

        if (roomNumber != null && !roomNumber.isBlank()) {
            spec = spec.and((root, query, cb) ->
                    cb.like(root.get("roomNumber"), "%" + roomNumber + "%"));
        }

        return roomRepository.findAll(spec, pageable);
    }

    /* ======================================================
                       FIND ROOM
    ====================================================== */

    @Transactional
    public Room findRoomById(Long id) {
        return roomRepository.findById(id).orElse(null);
    }

    @Transactional
    public List<String> getRoomNumbers() {

        List<Room> rooms = roomRepository.findAll();

        List<String> numbers = new ArrayList<>();

        for (Room r : rooms) {
            numbers.add(r.getRoomNumber());
        }

        return numbers;
    }

    @Transactional
    public void softDeleteRoom(Long id) {

        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        if (room.getStatus().equals(RoomStatus.AVAILABLE)) {
            room.setStatus(RoomStatus.DELETED);
            room.setDeleted(true);
        }

        roomRepository.save(room);
    }

    public void deleteRooms(List<Long> ids) {
        for (Long id : ids) {
            Room room = roomRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Room not found"));
            roomRepository.deleteById(id);
        }
    }

    /* ======================================================
                       EDIT ROOM
    ====================================================== */

    @Transactional
    public void editRoom(
            Long roomId,
            BigDecimal basePrice,
            BigDecimal overPrice,
            RoomStatus status,
            Long roomTypeId,
            String roomTypeDescription,
            List<Long> amenityIds,
            List<String> newAmenityNames,
            List<MultipartFile> newAmenityIcons,
            MultipartFile[] images,
            String deletedImageIds
    ) {
        /* ===== 1. FIND ROOM ===== */
        if (roomId == null || roomId <= 0) {
            throw new IllegalArgumentException("ID phòng không hợp lệ");
        }

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phòng"));


        /* ===== 2. UPDATE ROOM ===== */
        if (basePrice == null || basePrice.compareTo(new BigDecimal("100000")) < 0) {
            throw new IllegalArgumentException("Giá / slot tối thiểu là 100.000₫");
        }

        if (basePrice.compareTo(new BigDecimal("5000000")) > 0) {
            throw new IllegalArgumentException("Giá / slot không vượt quá 5.000.000₫");
        }

        if (overPrice.compareTo(basePrice.add(new BigDecimal("100000"))) < 0) {
            throw new IllegalArgumentException("Giá / đêm phải cao hơn giá slot ít nhất 100.000₫");
        }

        if (overPrice.compareTo(basePrice) < 0) {
            throw new IllegalArgumentException("Giá phụ thu phải lớn hơn hoặc bằng giá cơ bản");
        }
        room.getRoomType().setBasePrice(basePrice);
        room.getRoomType().setOverPrice(overPrice);

        if (status == null) {
            throw new IllegalArgumentException("Trạng thái phòng là bắt buộc");
        }
        if (status.equals(RoomStatus.DELETED)) {
            throw new IllegalArgumentException("Không tùy chỉnh được trạng thái này");
        }
        room.setStatus(status);

        /* ===== 3. UPDATE ROOM TYPE ===== */

        if (roomTypeId == null || roomTypeId <= 0) {
            throw new IllegalArgumentException("Không tìm thấy loại phòng");
        }

        RoomType roomType = roomTypeRepository.findById(roomTypeId)
                .orElseThrow(() -> new RuntimeException("Loại phòng không hợp lệ"));

        room.setRoomType(roomType);

        if (roomTypeDescription != null) {
            roomTypeDescription = roomTypeDescription.trim();

            if (roomTypeDescription.length() > 500) {
                throw new IllegalArgumentException("Mô tả loại phòng quá dài");
            }

            roomType.setDescription(roomTypeDescription);
        }

        /* ===== 4. UPDATE AMENITIES ===== */

        roomAmenityRepository.deleteByRoomId(roomId);

        Set<Long> usedAmenityIds = new HashSet<>();

        if (amenityIds != null && !amenityIds.isEmpty()) {

            Set<Long> uniqueAmenityIds = new HashSet<>(amenityIds);

            List<Amenity> amenities = amenityRepository.findAllById(uniqueAmenityIds);

            if (amenities.size() != uniqueAmenityIds.size()) {
                throw new IllegalArgumentException("Một số tiện nghi không tồn tại");
            }

            for (Amenity amenity : amenities) {

                if (usedAmenityIds.contains(amenity.getAmenityId())) {
                    continue;
                }

                // ✅ đánh dấu đã dùng
                usedAmenityIds.add(amenity.getAmenityId());
                RoomAmenity ra = new RoomAmenity();
                ra.setRoom(room);
                ra.setAmenity(amenity);

                roomAmenityRepository.save(ra);
            }
        }

        /* ===== ADD NEW AMENITIES ===== */

        if (newAmenityNames != null && !newAmenityNames.isEmpty()) {

            if (newAmenityNames.size() > 10) {
                throw new IllegalArgumentException("Không được thêm quá 10 tiện nghi");
            }

            Set<String> uniqueNames = new HashSet<>();

            for (int i = 0; i < newAmenityNames.size(); i++) {

                String name = newAmenityNames.get(i).trim();

                if (name.length() < 2 || name.length() > 50) {
                    throw new IllegalArgumentException("Tên tiện nghi phải từ 2 đến 50 ký tự");
                }

                String normalized = name.trim().toLowerCase();

                if (!uniqueNames.add(normalized)) {
                    throw new IllegalArgumentException("Tên tiện nghi bị trùng");
                }

                Amenity amenity = amenityRepository.findByNameIgnoreCase(name);

                /* ===== CREATE IF NOT EXISTS ===== */

                if (amenity == null) {

                    amenity = new Amenity();
                    amenity.setName(name);

                    /* ===== VALIDATE ICON ===== */

                    if (newAmenityIcons == null || i >= newAmenityIcons.size()) {
                        throw new IllegalArgumentException("Tiện nghi mới phải có icon");
                    }

                    MultipartFile icon = newAmenityIcons.get(i);

                    if (icon == null || icon.isEmpty()) {
                        throw new IllegalArgumentException("Tiện nghi mới phải có icon");
                    }

                    /* ===== UPLOAD ICON ===== */

                    try {

                        if (icon.getSize() > 2_000_000) {
                            throw new IllegalArgumentException("Icon tiện nghi không được lớn hơn 2MB");
                        }

                        String contentType = icon.getContentType();

                        if (contentType == null || !contentType.startsWith("image/")) {
                            throw new IllegalArgumentException("File icon tiện nghi phải là hình ảnh");
                        }

                        Map upload = cloudinary.uploader().upload(
                                icon.getBytes(),
                                ObjectUtils.asMap("folder", "booknow/amenities")
                        );

                        String iconUrl = (String) upload.get("secure_url");

                        amenity.setIconUrl(iconUrl);

                    } catch (Exception e) {

                        throw new RuntimeException("Tải icon tiện nghi thất bại");

                    }

                    amenityRepository.save(amenity);
                }

                /* ===== LINK ROOM ===== */

                // ❌ nếu đã tồn tại thì bỏ qua
                if (usedAmenityIds.contains(amenity.getAmenityId())) {
                    continue;
                }

                // ✅ đánh dấu đã dùng
                usedAmenityIds.add(amenity.getAmenityId());

                RoomAmenity ra = new RoomAmenity();
                ra.setRoom(room);
                ra.setAmenity(amenity);

                roomAmenityRepository.save(ra);
            }
        }



        /* ===== 5. DELETE IMAGE ===== */


        if (deletedImageIds != null && !deletedImageIds.isBlank()) {

            List<Long> imageIds;

            try {

                imageIds = Arrays.stream(deletedImageIds.split(","))
                        .map(String::trim)
                        .map(Long::parseLong)
                        .toList();

            } catch (Exception e) {

                throw new IllegalArgumentException("Danh sách ảnh cần xóa không hợp lệ");

            }

            for (Long imageId : imageIds) {

                Image image = imageRepository.findById(imageId).orElse(null);

                if (image != null &&
                        image.getRoom() != null &&
                        image.getRoom().getRoomId().equals(roomId)) {

                    try {

                        // xóa trên cloudinary
                        if (image.getPublicId() != null) {
                            cloudinary.uploader().destroy(
                                    image.getPublicId(),
                                    ObjectUtils.emptyMap()
                            );
                        }

                    } catch (Exception e) {
                        System.out.println("Xóa ảnh trên Cloudinary thất bại: " + e.getMessage());
                    }

                    // xóa DB
                    imageRepository.delete(image);
                }
            }
        }

        /* ===== 6. UPLOAD IMAGE ===== */

        if (images != null && images.length > 5) {
            throw new IllegalArgumentException("Không được upload quá 5 ảnh");
        }

        if (images != null && images.length > 0) {

            for (MultipartFile file : images) {

                if (file == null || file.isEmpty()) {
                    continue;
                }

                if (file.getSize() > 2_000_000) {
                    throw new IllegalArgumentException("Ảnh không được lớn hơn 2MB");
                }

                String contentType = file.getContentType();

                if (contentType == null || !contentType.startsWith("image/")) {
                    throw new IllegalArgumentException("File upload phải là hình ảnh");
                }

                uploadImage(file, room, false);
            }
        }

        roomRepository.save(room);
    }

    /* ======================================================
                       CREATE ROOM
    ====================================================== */

    @Transactional
    public void createRoom(
            String roomNumber,
            Long roomTypeId,
            Long basePrice,
            Long overPrice,
            String description,
            List<Long> amenityIds,
            List<String> newAmenityNames,
            List<MultipartFile> newAmenityIcons,
            MultipartFile[] images
    ) {

        if (roomRepository.existsByRoomNumber(roomNumber)) {
            throw new RuntimeException("Số phòng đã tồn tại");
        }

        if (!roomNumber.matches("^[A-Z]-[A-Za-z]+-\\d{2}$")) {
            throw new RuntimeException("Số phòng không đúng định dạng (Ví dụ: G-Cyber-01)");
        }

        if (roomTypeId == null) {
            throw new RuntimeException("Vui lòng chọn loại phòng");
        }

        RoomType roomType = roomTypeRepository.findById(roomTypeId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy loại phòng"));

        if (roomType.getIsDeleted()) {
            throw new RuntimeException("Loại phòng đã bị xóa");
        }

        Room room = new Room();

        room.setRoomNumber(roomNumber);
        room.setStatus(RoomStatus.AVAILABLE);
        room.setRoomType(roomType);
        room.getRoomType().setIsDeleted(false);

        if (basePrice == null || basePrice < 100000) {
            throw new RuntimeException("Giá / slot tối thiểu là 100.000₫");
        }

        if (overPrice == null || overPrice < basePrice + 100000) {
            throw new RuntimeException("Giá quá giờ phải lớn hơn giá cơ bản ít nhất 100.000₫");
        }

        room.getRoomType().setBasePrice(basePrice != null ? BigDecimal.valueOf(basePrice) : BigDecimal.ZERO);
        room.getRoomType().setOverPrice(overPrice != null ? BigDecimal.valueOf(overPrice) : BigDecimal.ZERO);

        room.getRoomType().setDescription(description);
        roomRepository.save(room);

        /* ===== ADD AMENITIES ===== */

        if (amenityIds != null && !amenityIds.isEmpty()) {

            Set<Long> uniqueAmenityIds = new HashSet<>(amenityIds);

            List<Amenity> amenities = amenityRepository.findAllById(amenityIds);

            if (amenities.size() != uniqueAmenityIds.size()) {
                throw new IllegalArgumentException("Một số tiện nghi không tồn tại");
            }

            for (Amenity amenity : amenities) {

                RoomAmenity ra = new RoomAmenity();
                ra.setRoom(room);
                ra.setAmenity(amenity);

                roomAmenityRepository.save(ra);
            }
        }

        if (newAmenityNames != null && !newAmenityNames.isEmpty()) {

            for (int i = 0; i < newAmenityNames.size(); i++) {

                String name = newAmenityNames.get(i).trim();

                if (name.isBlank()) continue;

                Amenity amenity = amenityRepository.findByNameIgnoreCase(name);

                /* ===== CREATE IF NOT EXISTS ===== */

                if (amenity == null) {

                    amenity = new Amenity();
                    amenity.setName(name);

                    /* ===== UPLOAD ICON ===== */
                    if (newAmenityIcons != null && i < newAmenityIcons.size()) {

                        MultipartFile icon = newAmenityIcons.get(i);

                        String contentType = icon.getContentType();

                        if (!contentType.startsWith("image/")) {
                            throw new RuntimeException("Icon tiện nghi phải là hình ảnh");
                        }

                        if (icon != null && !icon.isEmpty()) {

                            try {

                                if (icon.getSize() > 2 * 1024 * 1024) {
                                    throw new RuntimeException("Ảnh phải nhỏ hơn 2MB");
                                }

                                Map upload = cloudinary.uploader().upload(
                                        icon.getBytes(),
                                        ObjectUtils.asMap("folder", "booknow/amenities")
                                );

                                String iconUrl = (String) upload.get("secure_url");

                                amenity.setIconUrl(iconUrl);

                            } catch (Exception e) {

                                throw new RuntimeException("Upload icon tiện nghi thất bại");

                            }
                        }
                    }

                    amenityRepository.save(amenity);
                }

                /* ===== LINK ROOM ===== */

                RoomAmenity ra = new RoomAmenity();
                ra.setRoom(room);
                ra.setAmenity(amenity);

                roomAmenityRepository.save(ra);
            }
        }



        /* ===== UPLOAD IMAGE ===== */

        if (images != null && images.length > 0) {

            if (images != null && images.length > 5) {
                throw new RuntimeException("Không được upload quá 5 ảnh");
            }

            boolean isFirst = true;

            for (MultipartFile file : images) {

                if (file == null || file.isEmpty()) {
                    continue;
                }

                uploadImage(file, room, isFirst);

                isFirst = false;
            }
        }
    }

    /* ======================================================
                       UPLOAD IMAGE
    ====================================================== */

    private Image uploadImage(MultipartFile file, Room room, boolean isCover) {

        try {
            if (file.getSize() > MAX_FILE_SIZE) {
                throw new IllegalArgumentException("Image must be <= 2MB");
            }

            if (!ALLOWED_TYPES.contains(file.getContentType())) {
                throw new IllegalArgumentException("Only JPEG, PNG, WebP allowed");
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap("folder", "booknow/rooms")
            );

            String imageUrl = (String) uploadResult.get("secure_url");
            String publicId = (String) uploadResult.get("public_id");

            if (imageUrl == null || publicId == null) {
                throw new RuntimeException("Upload image failed");
            }

            Image image = new Image();

            image.setImageUrl(imageUrl);
            image.setPublicId(publicId);
            image.setIsCover(isCover);
            image.setRoom(room);

            return imageRepository.save(image);

        } catch (Exception e) {

            throw new RuntimeException("Upload image failed", e);
        }
    }

    public DashboardDTO getDashboard(String startDate, String endDate) {

        DashboardDTO dto = new DashboardDTO();

    /* =====================
       DATE RANGE
       ===================== */

        LocalDate start;
        LocalDate end;

        if (startDate == null || endDate == null || startDate.isBlank() || endDate.isBlank()) {
            start = LocalDate.now().withDayOfMonth(1);
            end = LocalDate.now();
        } else {
            try {
                String normalizedStart = startDate.length() >= 10 ? startDate.substring(0, 10) : startDate;
                String normalizedEnd = endDate.length() >= 10 ? endDate.substring(0, 10) : endDate;

                start = LocalDate.parse(normalizedStart);
                end = LocalDate.parse(normalizedEnd);
            } catch (Exception ex) {
                // If parsing fails (bad format), fall back to current month range
                start = LocalDate.now().withDayOfMonth(1);
                end = LocalDate.now();
            }
        }

        // Ensure the range is valid
        if (start.isAfter(end)) {
            LocalDate tmp = start;
            start = end;
            end = tmp;
        }

        LocalDate today = LocalDate.now();

        if (end.isAfter(today)) {
            end = today;
        }

        LocalDateTime startTime = start.atStartOfDay();
        LocalDateTime endTime = end.atTime(23, 59, 59);

    /* =====================
       BOOKING COUNT
       ===================== */

        int bookingCount =
                bookingRepository.countByCreatedAtBetweenAndBookingStatusNot(
                        startTime,
                        endTime,
                        "DELETED"
                );

    /* =====================
       REVENUE
       ===================== */

        long revenue = bookingRepository.sumRevenue(startTime, endTime);

    /* =====================
       ROOM STATS
       ===================== */

        int totalRooms =
                roomRepository.countByStatusNot(RoomStatus.DELETED);

        int activeRooms =
                roomRepository.countByStatusIn(
                        List.of("BOOKED", "OCCUPIED")
                );

    /* =====================
       PERIOD COMPARISON
       ===================== */

        long days =
                java.time.temporal.ChronoUnit.DAYS.between(start, end) + 1;

        String compareLabel;
        String chartTitle = "";

        if (days == 1) {
            compareLabel = "so với hôm qua";
        } else if (days <= 7) {
            compareLabel = "so với tuần trước";
        } else if (days <= 31) {
            compareLabel = "so với tháng trước";
        } else {
            compareLabel = "so với kỳ trước";
        }

    /* =====================
       PREVIOUS PERIOD
       ===================== */

        LocalDate prevStart = start.minusDays(days);
        LocalDate prevEnd = start.minusDays(1);

        LocalDateTime prevStartTime = prevStart.atStartOfDay();
        LocalDateTime prevEndTime = prevEnd.atTime(23, 59, 59);

    /* =====================
       PREVIOUS BOOKINGS
       ===================== */

        int prevBookings =
                bookingRepository.countByCreatedAtBetweenAndBookingStatusNot(
                        prevStartTime,
                        prevEndTime,
                        "DELETED"
                );

        double bookingPercent;

        if (prevBookings == 0) {

            if (bookingCount == 0) {
                bookingPercent = 0;
            } else {
                bookingPercent = 100;
            }

        } else {

            bookingPercent =
                    ((double) (bookingCount - prevBookings) / prevBookings) * 100;
        }

    /* =====================
       PREVIOUS REVENUE
       ===================== */
        long prevRevenue = bookingRepository.sumRevenue(prevStartTime, prevEndTime);

        double revenuePercent;

        if (prevRevenue == 0) {

            if (revenue == 0) {
                revenuePercent = 0;
            } else {
                revenuePercent = 100;
            }

        } else {

            revenuePercent =
                    ((double) (revenue - prevRevenue) / prevRevenue) * 100;
        }

    /* =====================
       CURRENT MONTH LABEL
       ===================== */

        String currentMonth =
                start.format(DateTimeFormatter.ofPattern("MM/yyyy"));
        /* =====================
       Table Status
       ===================== */
        Map<String, Integer> statusMap = new HashMap<>();

        for (Object[] row : bookingRepository.countByStatus(startTime, endTime)) {
            statusMap.put((String) row[0], ((Long) row[1]).intValue());
        }

        int paid = statusMap.getOrDefault("PAID", 0);
        int pending = statusMap.getOrDefault("PENDING", 0);
        int failed = statusMap.getOrDefault("FAILED", 0);

        // Lượng đặt phòng
        List<Integer> chartData = new ArrayList<>();
        List<String> chartLabels = new ArrayList<>();

        List<Long> revenueData = new ArrayList<>();
        List<String> revenueLabels = new ArrayList<>();


/* =====================
   ≤ 7 ngày → theo ngày
   ===================== */

        if (days <= 7) {

            LocalDate current = start;
            int week = start.get(java.time.temporal.IsoFields.WEEK_OF_WEEK_BASED_YEAR);

            Map<LocalDate, Integer> bookingMap = new HashMap<>();
            Map<LocalDate, Long> revenueMap = new HashMap<>();

            for (Object[] row : bookingRepository.countByDate(startTime, endTime)) {
                bookingMap.put(((java.sql.Date) row[0]).toLocalDate(), ((Long) row[1]).intValue());
            }

            for (Object[] row : bookingRepository.revenueByDate(startTime, endTime)) {
                revenueMap.put(((java.sql.Date) row[0]).toLocalDate(), ((Long) row[1]));
            }

            while (!current.isAfter(end)) {

                int count = bookingMap.getOrDefault(current, 0);
                long dayRevenue = revenueMap.getOrDefault(current, 0L);

                chartData.add(count);
                revenueData.add(dayRevenue);

                chartLabels.add(current.getDayOfMonth() + "/" + current.getMonthValue());
                revenueLabels.add(current.getDayOfMonth() + "/" + current.getMonthValue());

                current = current.plusDays(1);
            }
            chartTitle = "Tuần " + week + " năm " + start.getYear();

        } else if (days <= 31) {

            LocalDate current = start;

            Map<LocalDate, Integer> bookingMap = new HashMap<>();
            Map<LocalDate, Long> revenueMap = new HashMap<>();

            for (Object[] row : bookingRepository.countByDate(startTime, endTime)) {
                bookingMap.put(((java.sql.Date) row[0]).toLocalDate(), ((Long) row[1]).intValue());
            }

            for (Object[] row : bookingRepository.revenueByDate(startTime, endTime)) {
                revenueMap.put(((java.sql.Date) row[0]).toLocalDate(), ((Long) row[1]));
            }

            while (!current.isAfter(end)) {

                LocalDate weekEnd = current.plusDays(6);

                if (weekEnd.isAfter(end)) {
                    weekEnd = end;
                }

                int count = 0;
                long weekRevenue = 0;

                LocalDate temp = current;

                while (!temp.isAfter(weekEnd)) {
                    count += bookingMap.getOrDefault(temp, 0);
                    weekRevenue += revenueMap.getOrDefault(temp, 0L);
                    temp = temp.plusDays(1);
                }

                chartData.add(count);
                revenueData.add(weekRevenue);

                String label =
                        current.format(DateTimeFormatter.ofPattern("dd/MM"))
                                + " - " +
                                weekEnd.format(DateTimeFormatter.ofPattern("dd/MM"));

                chartLabels.add(label);
                revenueLabels.add(label);

                current = current.plusDays(7);
            }

            chartTitle = "Theo tuần";
        }

/* =====================
   ≤ 3 tháng → theo tháng
   ===================== */

        else if (days <= 90) {

            LocalDate current = start.withDayOfMonth(1);

            while (!current.isAfter(end)) {

                LocalDate monthEnd = current.withDayOfMonth(current.lengthOfMonth());

                if (monthEnd.isAfter(end)) {
                    monthEnd = end;
                }

                int count = bookingRepository.countByCreatedAtBetween(
                        current.atStartOfDay(),
                        monthEnd.atTime(23, 59, 59)
                );

                long monthRevenue = bookingRepository.sumRevenue(
                        current.atStartOfDay(),
                        monthEnd.atTime(23, 59, 59)
                );

                chartData.add(count);
                revenueData.add(monthRevenue);

                chartLabels.add("T" + current.getMonthValue() + "/" + current.getYear());
                revenueLabels.add("T" + current.getMonthValue() + "/" + current.getYear());

                current = current.plusMonths(1); // ⚠️ QUAN TRỌNG (tránh loop vô hạn)
            }

            chartTitle = "Từ " + start.getDayOfMonth() + "/" + start.getMonthValue() + "/" + start.getYear() +
                    " đến " + end.getDayOfMonth() + "/" + end.getMonthValue() + "/" + end.getYear();
        }

/* =====================
   > 3 tháng → theo quý
   ===================== */

        else {

            LocalDate current = start.withDayOfMonth(1);

            while (!current.isAfter(end)) {

                int quarter = (current.getMonthValue() - 1) / 3 + 1;

                LocalDate quarterStart =
                        LocalDate.of(current.getYear(), (quarter - 1) * 3 + 1, 1);

                LocalDate quarterEnd =
                        quarterStart.plusMonths(2)
                                .withDayOfMonth(
                                        quarterStart.plusMonths(2).lengthOfMonth()
                                );

                int count = bookingRepository.countByCreatedAtBetween(
                        quarterStart.atStartOfDay(),
                        quarterEnd.atTime(23, 59, 59)
                );

                long quarterRevenue =
                        bookingRepository.sumRevenue(
                                quarterStart.atStartOfDay(),
                                quarterEnd.atTime(23, 59, 59)
                        );

                chartData.add(count);
                chartLabels.add("Q" + quarter + " " + current.getYear());

                revenueData.add(quarterRevenue);
                revenueLabels.add("Q" + quarter + " " + current.getYear());

                current = current.plusMonths(3);
                chartTitle = "Quý " + quarter +
                        " năm " + start.getYear();
            }
        }
    /* =====================
       SET DTO
       ===================== */
        dto.setStatusData(List.of(paid, pending, failed));
        dto.setQuarterBookings(chartData);
        dto.setQuarterLabels(chartLabels);
        dto.setChartTitle(chartTitle);
        dto.setRevenueData(revenueData);
        dto.setRevenueLabels(revenueLabels);

        dto.setCompareLabel(compareLabel);
        dto.setCurrentMonth(currentMonth);

        dto.setBookingCount(bookingCount);
        dto.setRevenue(revenue);

        dto.setTotalRooms(totalRooms);
        dto.setActiveRooms(activeRooms);

        dto.setBookingPercent(bookingPercent);
        dto.setRevenuePercent(revenuePercent);

        return dto;
    }

    @Transactional
    public void exportCSV(String startDate, String endDate, HttpServletResponse response) throws Exception {

        LocalDateTime start = LocalDate.parse(startDate).atStartOfDay();
        LocalDateTime end = LocalDate.parse(endDate).atTime(23, 59, 59);

        List<Booking> bookings = bookingRepository
                .findByCheckOutTimeBetween(start, end);

        // header file
        response.setContentType("text/csv");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Content-Disposition",
                "attachment; filename=booking_report.csv");

        PrintWriter writer = response.getWriter();

        // FIX lỗi tiếng Việt Excel
        writer.write("\uFEFF");

        writer.println("Booking Code,Customer,Room,Check-in,Check-out,Status,Total");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        for (Booking b : bookings) {

            String bookingCode = b.getBookingCode();
            String customer = b.getCustomer().getFullName();
            String room = b.getRoom().getRoomNumber();
            String checkIn = b.getCheckInTime().format(formatter);
            String checkOut = b.getCheckOutTime().format(formatter);
            String status = b.getBookingStatus();
            BigDecimal total = b.getTotalAmount();

            writer.println(
                    bookingCode + "," +
                            customer + "," +
                            room + "," +
                            checkIn + "," +
                            checkOut + "," +
                            status + "," +
                            total
            );
        }

        writer.flush();
        writer.close();
    }

    @Transactional
    public void exportExcel(String startDate, String endDate, HttpServletResponse response) {

        try {

            LocalDateTime start = LocalDate.parse(startDate).atStartOfDay();
            LocalDateTime end = LocalDate.parse(endDate).atTime(23, 59, 59);

            List<Booking> bookings =
                    bookingRepository.findByCheckOutTimeBetween(start, end);

            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Bookings");

            Row headerRow = sheet.createRow(0);

            headerRow.createCell(0).setCellValue("Booking Code");
            headerRow.createCell(1).setCellValue("Customer");
            headerRow.createCell(2).setCellValue("Room");
            headerRow.createCell(3).setCellValue("Check-in");
            headerRow.createCell(4).setCellValue("Check-out");
            headerRow.createCell(5).setCellValue("Status");
            headerRow.createCell(6).setCellValue("Total");

            int rowNum = 1;

            DateTimeFormatter formatter =
                    DateTimeFormatter.ofPattern("dd/MM/yyyy");

            for (Booking b : bookings) {

                Row row = sheet.createRow(rowNum++);

                row.createCell(0).setCellValue(b.getBookingCode());
                row.createCell(1).setCellValue(b.getCustomer().getFullName());
                row.createCell(2).setCellValue(b.getRoom().getRoomNumber());
                row.createCell(3).setCellValue(b.getCheckInTime().format(formatter));
                row.createCell(4).setCellValue(b.getCheckOutTime().format(formatter));
                row.createCell(5).setCellValue(b.getBookingStatus());
                row.createCell(6).setCellValue(b.getTotalAmount().doubleValue());
            }

            response.setContentType(
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            );

            response.setHeader(
                    "Content-Disposition",
                    "attachment; filename=booking_report.xlsx"
            );

            workbook.write(response.getOutputStream());
            workbook.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Transactional
    public void exportPDF(String startDate, String endDate, HttpServletResponse response) {

        try {

            /* ===== PARSE DATE ===== */
            LocalDateTime start = LocalDate.parse(startDate).atStartOfDay();
            LocalDateTime end = LocalDate.parse(endDate).atTime(23, 59, 59);

            List<Booking> bookings =
                    bookingRepository.findByCheckOutTimeBetween(start, end);

            /* ===== RESPONSE HEADER ===== */
            response.setContentType("application/pdf");
            response.setHeader(
                    "Content-Disposition",
                    "attachment; filename=booking_report.pdf"
            );

            PdfWriter writer = new PdfWriter(response.getOutputStream());
            PdfDocument pdf = new PdfDocument(writer);

            /* ===== LOAD FONT (SUPPORT VIETNAMESE) ===== */

            String fontPath = new ClassPathResource("fonts/NotoSans-Regular.ttf")

                    .getFile()
                    .getAbsolutePath();
            PdfFont font = PdfFontFactory.createFont(
                    fontPath,
                    PdfEncodings.IDENTITY_H
            );

            Document document = new Document(pdf);
            document.setFont(font);

            /* ===== TITLE ===== */
            Paragraph title = new Paragraph("BOOKING REPORT")
                    .setBold()
                    .setFontSize(20)
                    .setTextAlignment(TextAlignment.CENTER);

            document.add(title);

            document.add(
                    new Paragraph("From " + startDate + " to " + endDate)
                            .setTextAlignment(TextAlignment.CENTER)
            );

            document.add(new Paragraph(" "));

            /* ===== TABLE ===== */
            Table table = new Table(
                    UnitValue.createPercentArray(new float[]{10, 25, 8, 12, 12, 13, 20})
            );

            table.setWidth(UnitValue.createPercentValue(100));

            /* ===== HEADER ===== */
            table.addHeaderCell(new Cell().add(new Paragraph("Booking Code").setBold()));
            table.addHeaderCell(new Cell().add(new Paragraph("Customer").setBold()));
            table.addHeaderCell(new Cell().add(new Paragraph("Room").setBold()));
            table.addHeaderCell(new Cell().add(new Paragraph("Check-in").setBold()));
            table.addHeaderCell(new Cell().add(new Paragraph("Check-out").setBold()));
            table.addHeaderCell(new Cell().add(new Paragraph("Status").setBold()));
            table.addHeaderCell(new Cell().add(new Paragraph("Total").setBold()));

            DateTimeFormatter formatter =
                    DateTimeFormatter.ofPattern("dd/MM/yyyy");

            long totalRevenue = 0;

            /* ===== DATA ===== */
            for (Booking b : bookings) {

                String customer =
                        b.getCustomer() != null
                                ? b.getCustomer().getFullName()
                                : "N/A";

                String room =
                        b.getRoom() != null
                                ? b.getRoom().getRoomNumber()
                                : "N/A";

                String checkIn =
                        b.getCheckInTime() != null
                                ? b.getCheckInTime().format(formatter)
                                : "N/A";

                String checkOut =
                        b.getCheckOutTime() != null
                                ? b.getCheckOutTime().format(formatter)
                                : "N/A";

                table.addCell(new Cell().add(new Paragraph(b.getBookingCode())));

                table.addCell(new Cell().add(new Paragraph(customer)));

                table.addCell(
                        new Cell()
                                .add(new Paragraph(room))
                                .setTextAlignment(TextAlignment.CENTER)
                );

                table.addCell(
                        new Cell()
                                .add(new Paragraph(checkIn))
                                .setTextAlignment(TextAlignment.CENTER)
                );

                table.addCell(
                        new Cell()
                                .add(new Paragraph(checkOut))
                                .setTextAlignment(TextAlignment.CENTER)
                );

                table.addCell(
                        new Cell()
                                .add(new Paragraph(b.getBookingStatus()))
                                .setTextAlignment(TextAlignment.CENTER)
                );

                long amount = b.getTotalAmount() != null
                        ? b.getTotalAmount().longValue()
                        : 0;

                totalRevenue += amount;

                String money = String.format("%,d", amount) + " VND";

                table.addCell(
                        new Cell()
                                .add(new Paragraph(money))
                                .setTextAlignment(TextAlignment.RIGHT)
                );
            }

            document.add(table);

            document.add(new Paragraph(" "));

            /* ===== SUMMARY ===== */
            document.add(
                    new Paragraph("Total Bookings: " + bookings.size())
                            .setBold()
            );

            document.add(
                    new Paragraph("Total Revenue: " +
                            String.format("%,d VND", totalRevenue))
                            .setBold()
            );

            document.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}