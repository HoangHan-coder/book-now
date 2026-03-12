package vn.edu.fpt.booknow.ui;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.BeforeAll;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * Base class cho tất cả Selenium UI tests.
 *
 * Chiến lược dùng chung phiên đăng nhập:
 * ─────────────────────────────────────────────────────────────────────────────
 *  • Lần đầu chạy bất kỳ test class nào kế thừa class này, trình duyệt sẽ
 *    tự mở và điều hướng đến trang admin.
 *  • Nếu Spring Security redirect sang trang login, hãy đăng nhập thủ công
 *    (bỏ qua CAPTCHA). Test sẽ đợi tối đa LOGIN_TIMEOUT_SECONDS giây.
 *  • Sau khi đăng nhập thành công, các test class khác reuse cùng WebDriver
 *    instance mà không cần đăng nhập lại.
 *  • Trình duyệt đóng tự động khi JVM tắt (shutdown hook).
 * ─────────────────────────────────────────────────────────────────────────────
 *
 * Yêu cầu:
 *  • Ứng dụng đang chạy tại BASE_URL (mặc định: http://localhost:8080)
 *  • Google Chrome đã được cài đặt
 */

public abstract class SeleniumBaseTest {

    /** URL gốc của ứng dụng đang chạy */
    protected static final String BASE_URL = "http://localhost:8080";

    /** Thời gian chờ đăng nhập thủ công (giây) */
    private static final int LOGIN_TIMEOUT_SECONDS = 120;

    /** Implicit wait mặc định cho mỗi thao tác tìm element (giây) */
    protected static final int ELEMENT_WAIT_SECONDS = 5;

    /** WebDriver instance dùng chung – static nên tồn tại xuyên suốt JVM */
    protected static WebDriver driver;

    /** Cờ đánh dấu đã hoàn tất vòng đời khởi tạo */
    private static boolean sessionInitialized = false;

    /**
     * Khởi tạo WebDriver và xử lý bước đăng nhập thủ công một lần duy nhất.
     * Phương thức này được kế thừa và gọi tự động trước test class con nào.
     */
    @BeforeAll
    static void initSession() {
        if (sessionInitialized) {
            return; // Đã khởi tạo từ test class trước → bỏ qua
        }

        // ── Cấu hình ChromeDriver ─────────────────────────────────────────────
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        // Thêm option nếu cần, ví dụ: options.addArguments("--start-maximized");
        driver = new ChromeDriver(options);
        driver.manage().window().maximize();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(ELEMENT_WAIT_SECONDS));

        // ── Đăng ký shutdown hook – đóng trình duyệt khi JVM tắt ────────────
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (driver != null) {
                try {
                    driver.quit();
                } catch (Exception ignored) {
                    // Bỏ qua lỗi khi đóng driver lúc tắt JVM
                }
            }
        }));

        // ── Điều hướng đến trang admin và đợi đăng nhập ──────────────────────
        driver.get(BASE_URL + "/admin/bookings");

        printLoginBanner();

        // Đợi cho đến khi URL không còn chứa "login" và chứa "/admin"
        new WebDriverWait(driver, Duration.ofSeconds(LOGIN_TIMEOUT_SECONDS))
                .until(d -> !d.getCurrentUrl().contains("login")
                        && d.getCurrentUrl().contains("/admin"));

        System.out.println(">>> [SeleniumBaseTest] Đăng nhập thành công! Bắt đầu chạy test UI...\n");
        sessionInitialized = true;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helper
    // ─────────────────────────────────────────────────────────────────────────

    private static void printLoginBanner() {
        System.out.println("\n" + "=".repeat(65));
        System.out.println("  [SeleniumBaseTest] Trình duyệt đã mở.");
        System.out.println("  Nếu bạn thấy trang ĐĂNG NHẬP, hãy đăng nhập thủ công.");
        System.out.println("  Test sẽ tự động tiếp tục sau khi phát hiện phiên admin.");
        System.out.println("  Thời gian chờ tối đa: " + LOGIN_TIMEOUT_SECONDS + " giây.");
        System.out.println("=".repeat(65) + "\n");
    }

    /**
     * Tiện ích: tạo WebDriverWait với timeout tuỳ chỉnh.
     *
     * @param seconds thời gian chờ tối đa
     * @return WebDriverWait instance
     */
    protected static WebDriverWait waitFor(int seconds) {
        return new WebDriverWait(driver, Duration.ofSeconds(seconds));
    }

    /**
     * Tiện ích: chờ URL chứa chuỗi mong đợi.
     *
     * @param urlFragment chuỗi cần xuất hiện trong URL
     * @param timeoutSeconds thời gian chờ tối đa
     */
    protected void waitForUrlContaining(String urlFragment, int timeoutSeconds) {
        new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds))
                .until(ExpectedConditions.urlContains(urlFragment));
    }
}
