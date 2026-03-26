package vn.edu.fpt.booknow.controllers.staff;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import vn.edu.fpt.booknow.model.dto.DashBoardStaffDTO;
import vn.edu.fpt.booknow.model.entities.Booking;
import vn.edu.fpt.booknow.model.entities.Room;
import vn.edu.fpt.booknow.services.DashBoardStaffService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping(value = "/staff")
public class StaffDashBoardController {
    @Autowired
    private DashBoardStaffService dashBoardStaffService;

    @GetMapping("/dashboard")
    public String staffdashboard(Model model) {

        // Thời gian cập nhật lần cuối
        String lastUpdate = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("HH:mm, dd/MM/yyyy"));

        LocalDate date = LocalDate.now();
//        LocalDate date = LocalDate.of(2026, 3, 30);

        // Gọi service duy nhất để lấy toàn bộ dữ liệu dashboard
        DashBoardStaffDTO dashboard = dashBoardStaffService.getDashboard(date);

        // Đưa dữ liệu vào model để hiển thị trong HTML
        model.addAttribute("dashboard", dashboard);
        model.addAttribute("lastUpdate", lastUpdate);

        return "/private/Staff_dashboard";
    }
}
