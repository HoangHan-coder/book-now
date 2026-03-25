package vn.edu.fpt.booknow.controllers.admin;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import vn.edu.fpt.booknow.exceptions.ResourceNotFoundException;
import vn.edu.fpt.booknow.model.dto.DashboardDTO;
import vn.edu.fpt.booknow.services.ManageRoomServices;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Controller
@RequestMapping("/admin")
public class AdminDashboardController {

    @Autowired
    private ManageRoomServices manageRoomServices;

    @GetMapping("/dashboard")
    public String adminDashboard(
            Model model,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {

        try {

            String lastUpdate = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("HH:mm, dd/MM/yyyy"));

            DashboardDTO data =
                    manageRoomServices.getDashboard(startDate, endDate);

            DateTimeFormatter displayFormat =
                    DateTimeFormatter.ofPattern("dd/MM/yyyy");

            LocalDate start;
            LocalDate end;

            if (startDate == null || endDate == null || startDate.isBlank() || endDate.isBlank()) {
                start = LocalDate.now().withDayOfMonth(1);
                end = LocalDate.now();
            } else {
                try {
                    // Accept full ISO date-time strings or plain dates
                    start = LocalDate.parse(startDate.length() >= 10 ? startDate.substring(0, 10) : startDate);
                    end = LocalDate.parse(endDate.length() >= 10 ? endDate.substring(0, 10) : endDate);
                } catch (Exception ex) {
                    // Fallback to defaults if parsing fails (e.g., invalid format)
                    start = LocalDate.now().withDayOfMonth(1);
                    end = LocalDate.now();
                }
            }

            String dateLabel =
                    start.format(displayFormat) + " – " + end.format(displayFormat);

            model.addAttribute("startDate", start);
            model.addAttribute("endDate", end);
            model.addAttribute("dashboard", data);
            model.addAttribute("lastUpdate", lastUpdate);
            model.addAttribute("dateLabel", dateLabel);

            return "private/Admin_dashboard";

        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @GetMapping("/dashboard/export/{type}")
    public void exportDashboard(
            @PathVariable String type,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            HttpServletResponse response) throws Exception {
        System.out.println("EXPORT CSV TRIGGERED" + type);
        switch (type.toLowerCase()) {
            case "csv":
                manageRoomServices.exportCSV(startDate, endDate, response);
                break;

            case "excel":
                manageRoomServices.exportExcel(startDate, endDate, response);
                break;
            default:
                throw new ResourceNotFoundException("Invalid export type: " + type);
        }
    }

}
