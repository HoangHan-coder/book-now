package vn.edu.fpt.booknow.ui;

import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.JavascriptExecutor;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

/**
 * Selenium UI tests – Luồng Staff tìm kiếm / lọc danh sách Booking.
 *
 * URL kiểm thử : GET /admin/bookings
 *
 * Yêu cầu trước khi chạy:
 *  • Ứng dụng phải đang chạy tại http://localhost:8080
 *  • Staff đã đăng nhập thủ công (SeleniumBaseTest sẽ nhắc nếu chưa đăng nhập)
 *
 * Test cases:
 *  UI-S01 – Trang danh sách load thành công, có form lọc
 *  UI-S02 – Có ít nhất 1 cột tiêu đề trong bảng
 *  UI-S03 – Nhập keyword không khớp → hiện thông báo "Không tìm thấy"
 *  UI-S04 – Nhập mã booking (một phần) → bảng hiện kết quả
 *  UI-S05 – Nhập tên khách hàng (một phần) → bảng hiện kết quả
 *  UI-S06 – Lọc theo trạng thái → bảng cập nhật
 *  UI-S07 – Nút "Lọc" submit form về đúng URL
 */
@DisplayName("UI – Staff Booking Search (Selenium)")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class StaffBookingSearchUITest extends SeleniumBaseTest {

    /** URL trang danh sách booking của staff */
    private static final String BOOKING_LIST_URL = BASE_URL + "/admin/bookings";

    /** Selector các element chính trên trang */
    private static final By FROM_DATE_INPUT = By.cssSelector("input[name='checkIn']");
    private static final By TO_DATE_INPUT   = By.cssSelector("input[name='checkOut']");
    private static final By KEYWORD_INPUT  = By.cssSelector("input[name='keyword']");
    private static final By STATUS_SELECT  = By.cssSelector("select[name='status']");
    private static final By FILTER_BUTTON  = By.cssSelector("button[type='submit']");
    private static final By TABLE_ROWS     = By.cssSelector("tbody tr");
    private static final By EMPTY_ROW_MSG  = By.cssSelector("tbody tr td[colspan='7']");

    // ─────────────────────────────────────────────────────────────────────────
    // Trước mỗi test: đảm bảo driver đang ở trang danh sách booking (không filter)
    // ─────────────────────────────────────────────────────────────────────────
    @BeforeEach
    void navigateToBookingList() {
        driver.get(BOOKING_LIST_URL);
        // Đợi form lọc xuất hiện → chứng tỏ trang đã load xong
        waitFor(30).until(ExpectedConditions.presenceOfElementLocated(FILTER_BUTTON));
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // UI-S01 – Trang load thành công, form lọc hiển thị đầy đủ
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @Order(1)
    @DisplayName("UI-S01 – Trang /admin/bookings load thành công và có form lọc")
    void pageLoadsSuccessfully_shouldShowFilterForm() {
        // Kiểm tra URL hiện tại
        String currentUrl = driver.getCurrentUrl();
        assertTrue(currentUrl.contains("/admin/bookings"),
                "URL phải chứa '/admin/bookings', thực tế: " + currentUrl);

        // Kiểm tra các input lọc xuất hiện
        assertTrue(driver.findElement(KEYWORD_INPUT).isDisplayed(),
                "Ô tìm kiếm keyword phải hiển thị");
        assertTrue(driver.findElement(STATUS_SELECT).isDisplayed(),
                "Dropdown trạng thái phải hiển thị");
        assertTrue(driver.findElement(FILTER_BUTTON).isDisplayed(),
                "Nút 'Lọc' phải hiển thị");
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // UI-S02 – Bảng có đầy đủ cột tiêu đề
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @Order(2)
    @DisplayName("UI-S02 – Bảng có đủ cột: Mã đặt phòng, Khách hàng, Số phòng, Trạng thái, Hành động")
    void tableHeaders_shouldContainRequiredColumns() {
        List<WebElement> headers = driver.findElements(By.cssSelector("thead th"));
        assertTrue(headers.size() >= 5, "Bảng phải có ít nhất 5 cột tiêu đề");

        // Lấy toàn bộ text header và kiểm tra từng cột quan trọng
        String headerText = headers.stream()
                .map(WebElement::getText)
                .reduce("", String::concat)
                .toLowerCase();

        assertTrue(headerText.contains("mã"), "Phải có cột mã đặt phòng");
        assertTrue(headerText.contains("khách"), "Phải có cột khách hàng");
        assertTrue(headerText.contains("trạng thái"), "Phải có cột trạng thái");
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // UI-S03 – Keyword không khớp → thông báo "Không tìm thấy"
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @Order(3)
    @DisplayName("UI-S03 – Nhập keyword không tồn tại → hiện dòng 'Không tìm thấy booking'")
    void searchWithNonexistentKeyword_shouldShowEmptyMessage() {
        WebElement keywordInput = driver.findElement(KEYWORD_INPUT);
        keywordInput.clear();
        keywordInput.sendKeys("KEYWORD_KHONG_BAO_GIO_TON_TAI_XYZ999");

        driver.findElement(FILTER_BUTTON).click();
        waitFor(30).until(ExpectedConditions.presenceOfElementLocated(TABLE_ROWS));

        // Tìm dòng thông báo "Không tìm thấy"
        List<WebElement> emptyMessages = driver.findElements(EMPTY_ROW_MSG);
        assertFalse(emptyMessages.isEmpty(),
                "Phải xuất hiện dòng thông báo 'Không tìm thấy booking' khi không có kết quả");

        String msg = emptyMessages.get(0).getText();
        assertTrue(msg.toLowerCase().contains("không tìm thấy"),
                "Nội dung thông báo phải chứa 'Không tìm thấy', thực tế: '" + msg + "'");
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // UI-S04 – Tìm theo một phần mã booking (BK)
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @Order(4)
    @DisplayName("UI-S04 – Nhập 'BK' vào ô tìm kiếm → bảng hiện kết quả chứa mã BK*")
    void searchByBookingCodePrefix_shouldReturnResults() {
        WebElement keywordInput = driver.findElement(KEYWORD_INPUT);
        keywordInput.clear();
        keywordInput.sendKeys("BK");

        driver.findElement(FILTER_BUTTON).click();
        waitFor(30).until(ExpectedConditions.presenceOfElementLocated(TABLE_ROWS));

        List<WebElement> rows = driver.findElements(TABLE_ROWS);

        // Nếu database trống thì skip – không thể kiểm tra giao diện với data rỗng
        assumeFalse(rows.isEmpty() || rows.get(0).getAttribute("colspan") != null,
                "[SKIP] Không có booking trong database – test UI-S04 bị bỏ qua");

        // Mỗi dòng trong bảng phải có mã bắt đầu bằng BK (cột đầu tiên)
        for (WebElement row : rows) {
            List<WebElement> cells = row.findElements(By.tagName("td"));
            if (cells.isEmpty()) continue;
            String bookingCode = cells.get(0).getText();
            assertTrue(bookingCode.toUpperCase().contains("BK"),
                    "Mã booking '" + bookingCode + "' phải chứa 'BK'");
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // UI-S05 – Tìm theo tên khách hàng
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @Order(5)
    @DisplayName("UI-S05 – Nhập tên khách vào ô tìm kiếm → bảng chỉ hiện booking của khách đó")
    void searchByCustomerName_shouldFilterResults() {
        // ── Bước 1: Lấy tên khách từ dòng đầu tiên trong bảng (nếu có) ────────
        List<WebElement> initialRows = driver.findElements(TABLE_ROWS);
        assumeFalse(initialRows.isEmpty(),
                "[SKIP] Không có booking trong database – test UI-S05 bị bỏ qua");

        // Cột thứ 2 (index 1) chứa tên khách hàng (trong thẻ p.text-sm)
        List<WebElement> cells = initialRows.get(0).findElements(By.tagName("td"));
        assumeFalse(cells.size() < 2,
                "[SKIP] Cấu trúc bảng không đủ cột – test UI-S05 bị bỏ qua");

        String customerName = cells.get(1)
                .findElement(By.cssSelector("p.\\text-sm")) // text-sm paragraph = tên khách
                .getText()
                .trim();

        // Nếu không extract được tên, dùng một ký tự đầu để tìm
        if (customerName.isEmpty()) {
            customerName = cells.get(1).getText().trim().split("\\n")[0];
        }

        assumeFalse(customerName.isEmpty(),
                "[SKIP] Không lấy được tên khách hàng để tìm kiếm");

        // ── Bước 2: Nhập tên khách và submit ─────────────────────────────────
        WebElement keywordInput = driver.findElement(KEYWORD_INPUT);
        keywordInput.clear();
        keywordInput.sendKeys(customerName);
        driver.findElement(FILTER_BUTTON).click();

        waitFor(30).until(ExpectedConditions.presenceOfElementLocated(TABLE_ROWS));

        // ── Bước 3: Kiểm tra kết quả trả về ──────────────────────────────────
        List<WebElement> resultRows = driver.findElements(TABLE_ROWS);

        // Phải có ít nhất 1 kết quả
        assumeFalse(resultRows.isEmpty(),
                "[SKIP] Tìm kiếm không trả về kết quả – database có thể trống");

        // Kết quả đầu tiên phải chứa tên khách đã tìm
        String firstResultCell = resultRows.get(0)
                .findElements(By.tagName("td"))
                .get(1)
                .getText();
        assertTrue(firstResultCell.toLowerCase().contains(customerName.toLowerCase()),
                "Kết quả tìm kiếm phải chứa tên khách '" + customerName + "'");
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // UI-S06 – Lọc theo trạng thái PAID
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @Order(6)
    @DisplayName("UI-S06 – Chọn trạng thái 'Đã thanh toán' (PAID) → bảng cập nhật theo filter")
    void filterByStatus_shouldUpdateTable() {
        Select statusSelect = new Select(driver.findElement(STATUS_SELECT));
        statusSelect.selectByValue("PAID");

        driver.findElement(FILTER_BUTTON).click();
        waitFor(30).until(ExpectedConditions.presenceOfElementLocated(TABLE_ROWS));

        // URL phải chứa status=PAID
        String currentUrl = driver.getCurrentUrl();
        assertTrue(currentUrl.contains("status=PAID"),
                "URL sau khi lọc phải chứa 'status=PAID', thực tế: " + currentUrl);

        // Nếu có kết quả, tất cả dòng phải có badge "Đã thanh toán"
        List<WebElement> rows = driver.findElements(TABLE_ROWS);
        List<WebElement> emptyMessages = driver.findElements(EMPTY_ROW_MSG);

        if (emptyMessages.isEmpty() && !rows.isEmpty()) {
            for (WebElement row : rows) {
                // Cột trạng thái (index 5)
                List<WebElement> cells = row.findElements(By.tagName("td"));
                if (cells.size() < 6) continue;
                String statusText = cells.get(5).getText();
                assertTrue(statusText.contains("Đã thanh toán"),
                        "Trạng thái phải là 'Đã thanh toán', thực tế: '" + statusText + "'");
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // UI-S07 – Nút "Lọc" submit form về đúng URL
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @Order(7)
    @DisplayName("UI-S07 – Nhấn nút 'Lọc' → URL chứa parameter keyword")
    void clickFilterButton_shouldNavigateWithParams() {
        WebElement keywordInput = driver.findElement(KEYWORD_INPUT);
        keywordInput.clear();
        keywordInput.sendKeys("test_keyword");

        driver.findElement(FILTER_BUTTON).click();
        waitFor(30).until(ExpectedConditions.urlContains("keyword=test_keyword"));

        String url = driver.getCurrentUrl();
        assertTrue(url.contains("keyword=test_keyword"),
                "URL sau khi submit phải chứa 'keyword=test_keyword', thực tế: " + url);
        assertTrue(url.contains("/admin/bookings"),
                "Vẫn phải ở trang /admin/bookings");
    }
    @Test
    @Order(8)
    @DisplayName("UI-S08 – Lọc theo From Date")
    void filterByFromDate_shouldUpdateTable() {

        WebElement fromDate = driver.findElement(FROM_DATE_INPUT);

        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].value='2025-01-01';", fromDate);

        driver.findElement(FILTER_BUTTON).click();

        waitFor(20).until(ExpectedConditions.urlContains("checkIn="));

        String url = driver.getCurrentUrl();

        assertTrue(url.contains("checkIn=2025-01-01"),
                "URL phải chứa checkIn=2025-01-01, thực tế: " + url);
    }
    @Test
    @Order(9)
    @DisplayName("UI-S09 – Lọc theo To Date")
    void filterByToDate_shouldUpdateTable() {

        WebElement toDate = driver.findElement(TO_DATE_INPUT);

        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].value='2026-12-31';", toDate);

        driver.findElement(FILTER_BUTTON).click();

        waitFor(20).until(ExpectedConditions.urlContains("checkOut="));

        String url = driver.getCurrentUrl();

        assertTrue(url.contains("checkOut=2026-12-31"),
                "URL phải chứa checkOut=2026-12-31, thực tế: " + url);
    }
}
