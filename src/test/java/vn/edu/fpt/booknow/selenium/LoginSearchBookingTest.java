package vn.edu.fpt.booknow.selenium;


import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

public class LoginSearchBookingTest {

    @Test
    void testLoginSearchBooking() throws InterruptedException {

        WebDriverManager.chromedriver().setup();

        WebDriver driver = new ChromeDriver();
        driver.manage().window().maximize();

        // mở login page
        driver.get("http://localhost:8080/login");

        // login
        driver.findElement(By.id("email")).sendKeys("staff@gmail.com");
        driver.findElement(By.id("password")).sendKeys("123456");

        driver.findElement(By.cssSelector("button[type='submit']")).click();

        Thread.sleep(3000);

        // search booking
        driver.findElement(By.name("keyword")).sendKeys("BK");
        driver.findElement(By.cssSelector("button[type='submit']")).click();

        Thread.sleep(2000);

        // view booking detail
        driver.findElement(By.linkText("Xem chi tiết")).click();

        Thread.sleep(3000);

        driver.quit();
    }
}