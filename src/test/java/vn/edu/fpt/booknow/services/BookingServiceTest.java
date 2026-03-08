package vn.edu.fpt.booknow.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import vn.edu.fpt.booknow.model.dto.WorkShift;
import vn.edu.fpt.booknow.model.entities.Room;
import vn.edu.fpt.booknow.model.entities.RoomType;
import vn.edu.fpt.booknow.model.entities.Timetable;
import vn.edu.fpt.booknow.repositories.*;
import com.cloudinary.Cloudinary;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BookingServiceTest {

    @Autowired
    private TimeTableRepository timetableRepository; // Repository thật để nạp cache
    private RoomRepository roomRepository;

    private BookingService bookingService;
    private static Map<String, Timetable> timetableCache = new HashMap<>();

    @BeforeAll
    void setup() {
        // Bây giờ có thể gọi repository thoải mái
        timetableRepository.findAll().forEach(t -> {
            timetableCache.put(t.getSlotName().split(" ")[0].trim(), t);
        });
    }

    @BeforeEach
    void setUp() {
        // Các repository khác có thể Mock
        roomRepository = mock(RoomRepository.class);
        BookingRepository bookingRepository = mock(BookingRepository.class);
        ScheduleRepository scheduleRepository = mock(ScheduleRepository.class);
        CustomerRepository customerRepository = mock(CustomerRepository.class);
        Cloudinary cloudinary = mock(Cloudinary.class);
        JWTService jwtService = mock(JWTService.class);

        // Chú ý: Truyền timetableRepository THẬT vào service nếu muốn nó dùng chung data với cache
        bookingService = new BookingService(
                bookingRepository, timetableRepository, roomRepository,
                scheduleRepository, customerRepository, cloudinary, jwtService
        );
    }

    /**
     * TEST 1: Kiểm tra Parse và Khung giờ đúng (4 ca)
     */
    @ParameterizedTest(name = "Test: {0}")
    @ValueSource(strings = {
            "08/03    Sáng (10:30 - 13:30)",    // ✅ Khớp DB
            "08/03 Sáng (19:30 - 21:00)",       // ❌ Lỗi: Sai khung giờ so với DB
            "08/03 San (10:30 - 13:30)",        // ❌ Lỗi: Sai tên ca
            "08/03 Sáng 10:30 - 13:30",          // ❌ Lỗi: Thiếu ngoặc
            "10/03 Sáng (21:00 - 09:50)",          // ❌ Lỗi: Sai khung giờ ca Sáng
    })
    void testWorkShift_Normalized(String input) {
        // --- BƯỚC 1: CHUẨN HÓA KHOẢNG TRẮNG ---
        String normalizedInput = input.trim().replaceAll("\\s+", " ");
        System.out.println("🔄 Chuẩn hóa: [" + normalizedInput + "]");

        // --- BƯỚC 2: KIỂM TRA ĐỊNH DẠNG DẤU NGOẶC ---
        if (!normalizedInput.contains("(") || !normalizedInput.contains(")")) {
            System.err.println("❌ Lỗi định dạng: Thiếu dấu ngoặc đơn.");
            return;
        }

        // --- BƯỚC 3: TÁCH DỮ LIỆU INPUT ---
        String dateStr = normalizedInput.split(" ")[0];
        String shiftInfoFromInput = normalizedInput.substring(dateStr.length() + 1); // "Sáng (10:30 - 13:30)"
        String shiftName = shiftInfoFromInput.split(" ")[0]; // "Sáng"

        // Tách giờ từ input (Lấy phần trong ngoặc)
        String timeInInput = shiftInfoFromInput.substring(shiftInfoFromInput.indexOf("(") + 1, shiftInfoFromInput.indexOf(")"));
        String startTimeInput = timeInInput.split("-")[0].trim(); // "10:30"
        String endTimeInput = timeInInput.split("-")[1].trim();   // "13:30"

        // --- BƯỚC 4: LẤY DỮ LIỆU TỪ DB CACHE ---
        Timetable dbData = timetableCache.get(shiftName);
        if (dbData == null) {
            System.err.println("❌ Lỗi: Tên ca '" + shiftName + "' không tồn tại trong DB.");
            return;
        }

        // --- BƯỚC 5: TÁCH GIỜ TỪ slotName TRONG DB ---
        // Giả sử slotName DB là "Sáng (10:30 - 13:30)"
        String dbSlotName = dbData.getSlotName();
        String timeInDB = dbSlotName.substring(dbSlotName.indexOf("(") + 1, dbSlotName.indexOf(")"));
        String startTimeDB = timeInDB.split("-")[0].trim(); // "10:30"
        String endTimeDB = timeInDB.split("-")[1].trim();   // "13:30"

        // --- BƯỚC 6: SO SÁNH CHI TIẾT ---
        boolean isStartMatch = startTimeInput.equals(startTimeDB);
        boolean isEndMatch = endTimeInput.equals(endTimeDB);

        if (isStartMatch && isEndMatch) {
            System.out.println("✅ Khớp DB: Ca " + shiftName + " [" + startTimeDB + " - " + endTimeDB + "]");
        } else {
            if (!isStartMatch) System.err.println("❌ Sai giờ bắt đầu: Nhập " + startTimeInput + " - DB yêu cầu " + startTimeDB);
            if (!isEndMatch) System.err.println("❌ Sai giờ kết thúc: Nhập " + endTimeInput + " - DB yêu cầu " + endTimeDB);
        }

        // --- BƯỚC 7: KIỂM TRA NGÀY ---
        validateDateRange(dateStr);
    }
    /**
     * Hàm hỗ trợ kiểm tra khoảng ngày
     */
    private void validateDateRange(String dateStr) {
        try {
            LocalDate today = LocalDate.now();
            String[] dateParts = dateStr.split("/");
            LocalDate workDate = LocalDate.of(today.getYear(), Integer.parseInt(dateParts[1]), Integer.parseInt(dateParts[0]));

            LocalDate startValid = today.plusDays(1);
            LocalDate endValid = today.plusDays(7);

            if (workDate.isBefore(startValid) || workDate.isAfter(endValid)) {
                System.err.println("❌ Nằm ngoài phạm vi cho phép");
            } else {
                System.out.println("✅ Ngày hợp lệ.");
            }
        } catch (Exception e) {
            System.err.println("❌ Lỗi: Định dạng ngày không hợp lệ.");
        }
    }
    /**
     * TEST 2: Kiểm tra tính tổng tiền (CÓ IN KẾT QUẢ RA CONSOLE)
     */
    @Test
    void testCalculateTotalAmount_Combinations() {
        // Thiết lập giá: Thường 100k, Đêm 150k
        setupRoomMock("100000", "150000");

        System.out.println("--- KẾT QUẢ KIỂM TRA TÍNH TIỀN ---");
        assertAmount(List.of("Sáng"), 100000L);
        assertAmount(List.of("Đêm"), 150000L);
        assertAmount(List.of("Sáng", "Chiều", "Tối"), 300000L);
        assertAmount(List.of("Sáng", "Chiều", "Tối", "Đêm"), 450000L);
        System.out.println("---------------------------------");
    }

    /**
     * TEST 3: Bắt lỗi khi sai khung giờ (4 ca)
     */
    @ParameterizedTest
    @CsvSource({
            "Sáng, 09:00, 12:00",
            "Chiều, 13:00, 16:00",
            "Tối, 18:00, 21:00",
            "Đêm, 20:00, 07:00"
    })
    void testValidateWorkShiftTime_AllWrongShifts_ShouldReturnErrors(String type, String startStr, String endStr) {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        WorkShift wrongShift = new WorkShift(tomorrow.atStartOfDay(), tomorrow.atTime(LocalTime.parse(startStr)), tomorrow.atTime(LocalTime.parse(endStr)), type);

        String error = invokeValidateWorkShiftTime(wrongShift);

        assertNotNull(error);
        System.out.println("Bắt lỗi thành công ca " + type + " (Giờ sai: " + startStr + "-" + endStr + "): " + error);
    }

    // --- HELPERS ---

    private void setupRoomMock(String base, String over) {
        Room mockRoom = new Room();
        RoomType type = new RoomType();
        type.setBasePrice(new BigDecimal(base));
        type.setOverPrice(new BigDecimal(over));
        mockRoom.setRoomType(type);
        when(roomRepository.findById(1L)).thenReturn(Optional.of(mockRoom));
    }

    private void assertAmount(List<String> types, Long expectedTotal) {
        List<WorkShift> group = types.stream()
                .map(t -> new WorkShift(null, null, null, t))
                .collect(Collectors.toList());

        Long actual = invokeCalculateTotalAmount(group, 1L);

        // In kết quả ra màn hình để bạn xem
        System.out.println("Test Combo " + types + " | Kỳ vọng: " + expectedTotal + " | Thực tế: " + actual);

        assertEquals(expectedTotal, actual);
    }

    private String invokeValidateWorkShiftTime(WorkShift shift) {
        try {
            java.lang.reflect.Method method = BookingService.class.getDeclaredMethod("validateWorkShiftTime", WorkShift.class);
            method.setAccessible(true);
            return (String) method.invoke(bookingService, shift);
        } catch (Exception e) { return null; }
    }

    private Long invokeCalculateTotalAmount(List<WorkShift> group, Long roomId) {
        try {
            java.lang.reflect.Method method = BookingService.class.getDeclaredMethod("calculateTotalAmount", List.class, Long.class);
            method.setAccessible(true);
            return (Long) method.invoke(bookingService, group, roomId);
        } catch (Exception e) { return 0L; }
    }
}