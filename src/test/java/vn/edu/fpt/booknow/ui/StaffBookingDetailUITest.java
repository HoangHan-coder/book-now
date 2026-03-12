
package vn.edu.fpt.booknow.ui;

import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

@DisplayName("UI – Staff Booking Detail (Selenium)")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class StaffBookingDetailUITest extends SeleniumBaseTest {

    private static final String BOOKING_LIST_URL = BASE_URL + "/admin/bookings";
    private static final String BOOKING_DETAIL_PATH = BASE_URL + "/admin/booking-detail/";

    /** Booking code dùng chung cho toàn bộ test */
    private static String extractedBookingCode;

    // ─────────────────────────────────────────
    // Extract booking code từ dòng đầu tiên
    // ─────────────────────────────────────────

    @BeforeAll
    static void extractBookingCode() {

        driver.get(BOOKING_LIST_URL);

        try {
            waitFor(20).until(
                    ExpectedConditions.presenceOfElementLocated(
                            By.cssSelector("table tbody tr"))
            );
        } catch (Exception e) {
            System.out.println("[TEST] Không tìm thấy bảng booking.");
            return;
        }

        List<WebElement> rows = driver.findElements(By.cssSelector("table tbody tr"));

        if (rows.isEmpty()) {
            System.out.println("[TEST] Database không có booking.");
            return;
        }

        List<WebElement> cells = rows.get(0).findElements(By.tagName("td"));

        if (!cells.isEmpty()) {
            extractedBookingCode = cells.get(0).getText().trim();
            System.out.println("[TEST] Booking dùng để test: " + extractedBookingCode);
        }
    }

    // ─────────────────────────────────────────
    // Trước mỗi test → vào trang booking detail
    // ─────────────────────────────────────────

    @BeforeEach
    void openDetailPage() {

        assumeFalse(
                extractedBookingCode == null || extractedBookingCode.isBlank(),
                "[SKIP] Database không có booking để test"
        );

        driver.get(BOOKING_DETAIL_PATH + extractedBookingCode);

        waitFor(20).until(
                ExpectedConditions.presenceOfElementLocated(By.tagName("h1"))
        );
    }

    // ═════════════════════════════════════════
    // UI-D01 – Click xem chi tiết
    // ═════════════════════════════════════════

    @Test
    @Order(1)
    @DisplayName("UI-D01 – Click 'Xem chi tiết' → điều hướng đúng URL")
    void clickViewDetail_shouldNavigateToDetailPage() {

        driver.get(BOOKING_LIST_URL);

        waitFor(20).until(
                ExpectedConditions.presenceOfElementLocated(
                        By.partialLinkText("Xem chi tiết"))
        );

        List<WebElement> detailLinks =
                driver.findElements(By.partialLinkText("Xem chi tiết"));

        assertFalse(detailLinks.isEmpty(),
                "Phải có ít nhất 1 link 'Xem chi tiết'");

        detailLinks.get(0).click();

        waitFor(20).until(
                ExpectedConditions.urlContains("/admin/booking-detail/")
        );

        assertTrue(driver.getCurrentUrl().contains("/admin/booking-detail/"));
    }

    // ═════════════════════════════════════════
    // UI-D02 – Header hiển thị booking code
    // ═════════════════════════════════════════

    @Test
    @Order(2)
    @DisplayName("UI-D02 – Header hiển thị booking code")
    void detailPage_shouldShowBookingCodeInHeader() {

        WebElement header = driver.findElement(By.tagName("h1"));

        String text = header.getText();

        assertFalse(text.isBlank());

        assertTrue(text.contains(extractedBookingCode),
                "Header phải chứa booking code");
    }

    // ═════════════════════════════════════════
    // UI-D03 – Thông tin khách hàng
    // ═════════════════════════════════════════

    @Test
    @Order(3)
    @DisplayName("UI-D03 – Hiển thị thông tin khách hàng")
    void detailPage_shouldShowCustomerInfo() {

        String page = driver.getPageSource();

        assertTrue(page.contains("SĐT") || page.contains("Phone"));
        assertTrue(page.contains("Email"));
        assertTrue(page.contains("Khách hàng"));

        List<WebElement> names = driver.findElements(By.tagName("h4"));

        assertFalse(names.isEmpty());

        String name = names.get(0).getText().trim();

        assertFalse(name.isBlank());
    }

    // ═════════════════════════════════════════
    // UI-D04 – Thông tin phòng
    // ═════════════════════════════════════════

    @Test
    @Order(4)
    @DisplayName("UI-D04 – Hiển thị thông tin phòng")
    void detailPage_shouldShowRoomInfo() {

        String page = driver.getPageSource();

        assertTrue(page.contains("Số phòng"));
        assertTrue(page.contains("Loại phòng"));
        assertTrue(page.contains("Thông tin phòng"));
    }

    // ═════════════════════════════════════════
    // UI-D05 – Thời gian lưu trú
    // ═════════════════════════════════════════

    @Test
    @Order(5)
    @DisplayName("UI-D05 – Hiển thị thời gian lưu trú")
    void detailPage_shouldShowDurationText() {

        String page = driver.getPageSource();

        assertTrue(page.contains("Thời gian lưu trú"));

        List<WebElement> duration =
                driver.findElements(By.cssSelector("p.font-semibold.text-blue-600"));

        assertFalse(duration.isEmpty());

        String text = duration.get(0).getText().trim();

        assertFalse(text.isBlank());

        assertTrue(
                text.contains("giờ") ||
                        text.contains("phút") ||
                        text.contains("xác định")
        );
    }

    // ═════════════════════════════════════════
    // UI-D06 – Nút quay lại
    // ═════════════════════════════════════════

    @Test
    @Order(6)
    @DisplayName("UI-D06 – Nút quay lại về danh sách")
    void clickBackButton_shouldNavigateToBookingList() {

        List<WebElement> back =
                driver.findElements(By.cssSelector("a[href*='/admin/bookings']"));

        assertFalse(back.isEmpty());

        back.get(0).click();

        waitFor(10).until(
                ExpectedConditions.urlContains("/admin/bookings")
        );

        assertTrue(driver.getCurrentUrl().contains("/admin/bookings"));
    }

    // ═════════════════════════════════════════
    // UI-D07 – Booking không tồn tại
    // ═════════════════════════════════════════

    @Test
    @Order(7)
    @DisplayName("UI-D07 – Booking không tồn tại → trang lỗi")
    void accessInvalidBookingCode_shouldShowErrorPage() {

        driver.get(BOOKING_DETAIL_PATH + "BK_KHONG_TON_TAI_9999");

        waitFor(10).until(
                ExpectedConditions.presenceOfElementLocated(By.tagName("body"))
        );

        String page = driver.getPageSource().toLowerCase();

        boolean error =
                page.contains("không tìm thấy") ||
                        page.contains("404") ||
                        page.contains("error") ||
                        page.contains("lỗi");

        assertTrue(error);
    }
}

