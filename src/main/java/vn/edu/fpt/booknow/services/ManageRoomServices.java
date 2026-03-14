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
            String status,
            String type,
            String roomNumber,
            Pageable pageable
    ) {

        Specification<Room> spec = (root, query, cb) -> cb.conjunction();

        if (status != null && !status.isBlank()) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("status"), status));
        }

        if (type != null && !type.isBlank()) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("roomType").get("name"), type));
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
    public void softDeleteRoom(Long id) {

        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        if(room.getStatus().equals("AVAILABLE")){
            room.setStatus("DELETED");
        }

        roomRepository.save(room);
    }

    /* ======================================================
                       EDIT ROOM
    ====================================================== */

    @Transactional
    public void editRoom(
            Long roomId,
            BigDecimal basePrice,
            BigDecimal overPrice,
            String status,
            Long roomTypeId,
            String roomTypeDescription,
            List<Long> amenityIds,
            List<String> newAmenityNames,
            List<MultipartFile> newAmenityIcons,
            MultipartFile[] images,
            String deletedImageIds
    ) {
        /* ===== 1. FIND ROOM ===== */

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        /* ===== 2. UPDATE ROOM ===== */

        room.getRoomType().setBasePrice(basePrice);
        room.getRoomType().setOverPrice(overPrice);
        room.setStatus(status);

        /* ===== 3. UPDATE ROOM TYPE ===== */

        RoomType roomType = roomTypeRepository.findById(roomTypeId)
                .orElseThrow(() -> new RuntimeException("RoomType not found"));

        room.setRoomType(roomType);
        room.getRoomType().setDescription(roomTypeDescription);

        /* ===== 4. UPDATE AMENITIES ===== */

        roomAmenityRepository.deleteByRoom(room);
        room.getRoomAmenities().clear();

        if (amenityIds != null && !amenityIds.isEmpty()) {

            List<Amenity> amenities = amenityRepository.findAllById(amenityIds);

            for (Amenity amenity : amenities) {

                RoomAmenity ra = new RoomAmenity();
                ra.setRoom(room);
                ra.setAmenity(amenity);

                roomAmenityRepository.save(ra);
                room.getRoomAmenities().add(ra);
            }
        }

        /* ===== ADD NEW AMENITIES ===== */
        System.out.println("Amenity names: " + newAmenityNames);
        System.out.println("Amenity icons: " + newAmenityIcons);

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
                        System.out.println("File name: " + icon.getOriginalFilename());
                        System.out.println("File size: " + icon.getSize());
                        System.out.println("Is empty: " + icon.isEmpty());

                        if (icon != null && !icon.isEmpty()) {

                            try {

                                Map upload = cloudinary.uploader().upload(
                                        icon.getBytes(),
                                        ObjectUtils.asMap("folder", "booknow/amenities")
                                );

                                String iconUrl = (String) upload.get("secure_url");

                                amenity.setIconUrl(iconUrl);

                            } catch (Exception e) {

                                throw new RuntimeException("Upload amenity icon failed");

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



        /* ===== 5. DELETE IMAGE ===== */

        if (deletedImageIds != null && !deletedImageIds.isBlank()) {

            List<Long> imageIds = Arrays.stream(deletedImageIds.split(","))
                    .map(String::trim)
                    .map(Long::parseLong)
                    .toList();

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
                        System.out.println("Cloudinary delete failed: " + e.getMessage());
                    }

                    // xóa DB
                    imageRepository.delete(image);
                }
            }
        }

        /* ===== 6. UPLOAD IMAGE ===== */

        if (images != null && images.length > 0) {

            for (MultipartFile file : images) {

                if (file == null || file.isEmpty()) {
                    continue;
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
            String status,
            String description,
            List<Long> amenityIds,
            List<String> newAmenityNames,
            MultipartFile[] images
    ) {

        if (roomRepository.existsByRoomNumber(roomNumber)) {
            throw new RuntimeException("Room already exists");
        }

        RoomType roomType = roomTypeRepository.findById(roomTypeId)
                .orElseThrow(() -> new RuntimeException("RoomType not found"));

        Room room = new Room();

        room.setRoomNumber(roomNumber);
        room.setStatus(status);
        room.setRoomType(roomType);
        room.getRoomType().setIsDeleted(false);

        room.getRoomType().setBasePrice(basePrice != null ? BigDecimal.valueOf(basePrice) : BigDecimal.ZERO);
        room.getRoomType().setOverPrice(overPrice != null ? BigDecimal.valueOf(overPrice) : BigDecimal.ZERO);

        room.getRoomType().setDescription(description);

        roomRepository.save(room);

        /* ===== ADD AMENITIES ===== */

        if (amenityIds != null && !amenityIds.isEmpty()) {

            List<Amenity> amenities = amenityRepository.findAllById(amenityIds);

            for (Amenity amenity : amenities) {

                RoomAmenity ra = new RoomAmenity();
                ra.setRoom(room);
                ra.setAmenity(amenity);

                roomAmenityRepository.save(ra);
            }
        }

        if (newAmenityNames != null) {

            for (String name : newAmenityNames) {

                name = name.trim();

                Amenity amenity = amenityRepository.findByNameIgnoreCase(name);

                if (amenity == null) {
                    amenity = new Amenity();
                    amenity.setName(name);
                    amenityRepository.save(amenity);
                }

                RoomAmenity ra = new RoomAmenity();
                ra.setRoom(room);
                ra.setAmenity(amenity);

                roomAmenityRepository.save(ra);
            }
        }

        /* ===== UPLOAD IMAGE ===== */

        if (images != null && images.length > 0) {

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

        if (startDate == null || endDate == null) {
            start = LocalDate.now().withDayOfMonth(1);
            end = LocalDate.now();
        } else {
            start = LocalDate.parse(startDate);
            end = LocalDate.parse(endDate);
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

        List<Booking> bookings =
                bookingRepository.findByCheckOutTimeBetween(
                        startTime,
                        endTime
                );

        long revenue = bookings.stream()
                .mapToLong(b -> b.getTotalAmount().longValue())
                .sum();

    /* =====================
       ROOM STATS
       ===================== */

        int totalRooms =
                roomRepository.countByStatusNot("DELETED");

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
        String chartTitle="";

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

        List<Booking> prevBookingsRevenue =
                bookingRepository.findByCheckOutTimeBetween(
                        prevStartTime,
                        prevEndTime
                );

        long prevRevenue = prevBookingsRevenue.stream()
                .mapToLong(b -> b.getTotalAmount().longValue())
                .sum();

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
        int paid = bookingRepository.countByBookingStatusAndCreatedAtBetween("PAID", startTime, endTime);
        int pending = bookingRepository.countByBookingStatusAndCreatedAtBetween("PENDING", startTime, endTime);
        int failed = bookingRepository.countByBookingStatusAndCreatedAtBetween("FAILED", startTime, endTime);

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

            while (!current.isAfter(end)) {

                LocalDateTime dayStart = current.atStartOfDay();
                LocalDateTime dayEnd = current.atTime(23,59,59);

                int count = bookingRepository.countByCreatedAtBetween(
                        current.atStartOfDay(),
                        current.atTime(23,59,59)
                );

                List<Booking> dayBookings =
                        bookingRepository.findByCheckOutTimeBetween(dayStart, dayEnd);

                long dayRevenue = dayBookings.stream()
                        .mapToLong(b -> b.getTotalAmount().longValue())
                        .sum();


                chartData.add(count);
                revenueData.add(dayRevenue);

                chartLabels.add(current.getDayOfMonth() + "/" + current.getMonthValue());
                revenueLabels.add(current.getDayOfMonth() + "/" + current.getMonthValue());

                current = current.plusDays(1);
            }
            chartTitle = "Tuần " + week + " năm " + start.getYear();

        } else if (days <= 31) {

            LocalDate current = start;

            while (!current.isAfter(end)) {

                LocalDate weekEnd = current.plusDays(6);

                if (weekEnd.isAfter(end)) {
                    weekEnd = end;
                }

                int count = bookingRepository.countByCreatedAtBetween(
                        current.atStartOfDay(),
                        weekEnd.atTime(23,59,59)
                );

                long weekRevenue = bookingRepository
                        .findByCheckOutTimeBetween(
                                current.atStartOfDay(),
                                weekEnd.atTime(23,59,59)
                        )
                        .stream()
                        .mapToLong(b -> b.getTotalAmount().longValue())
                        .sum();

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

            while (!current.isAfter(end))

            chartTitle = "Từ " + start.getDayOfMonth() +  "/" + start.getMonthValue() + "/" + start.getYear() +
                    " đến " + end.getDayOfMonth()+  "/" + end.getMonthValue() + "/" + end.getYear();
        }

/* =====================
   > 3 tháng → theo quý
   ===================== */

        else {

            LocalDate current = start.withDayOfMonth(1);

            while (!current.isAfter(end)) {

                int quarter = (current.getMonthValue() - 1) / 3 + 1;

                LocalDate quarterStart =
                        LocalDate.of(current.getYear(), (quarter-1)*3+1, 1);

                LocalDate quarterEnd =
                        quarterStart.plusMonths(2)
                                .withDayOfMonth(
                                        quarterStart.plusMonths(2).lengthOfMonth()
                                );

                int count = bookingRepository.countByCreatedAtBetween(
                        quarterStart.atStartOfDay(),
                        quarterEnd.atTime(23,59,59)
                );

                long quarterRevenue = bookingRepository
                        .findByCheckOutTimeBetween(
                                quarterStart.atStartOfDay(),
                                quarterEnd.atTime(23,59,59)
                        )
                        .stream()
                        .mapToLong(b -> b.getTotalAmount().longValue())
                        .sum();

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
            LocalDateTime end = LocalDate.parse(endDate).atTime(23,59,59);

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
            System.out.println(fontPath);
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