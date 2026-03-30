package vn.edu.fpt.booknow.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import vn.edu.fpt.booknow.model.dto.MomoRequestDTO;
import vn.edu.fpt.booknow.model.dto.MomoResponseDTO;
import vn.edu.fpt.booknow.model.entities.*;
import vn.edu.fpt.booknow.services.ExtendBookingService;
import vn.edu.fpt.booknow.services.HousekeepingTaskService;
import vn.edu.fpt.booknow.services.BookingService;
import vn.edu.fpt.booknow.services.MomoPaymentService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping(value = "/housekeeping")
public class HouseKeepingController {
    @Autowired
    private HousekeepingTaskService housekeepingTaskService;
    @Autowired
    private BookingService bookingService;
    @Autowired
    private ExtendBookingService  extendBookingService;

    @Autowired
    private MomoPaymentService momoPaymentService;

    @GetMapping(value = "/task")
    public String task(Model model) {
        List<HousekeepingTask> housekeepingTask = housekeepingTaskService.getAllHousekeepingTask();
        model.addAttribute("housekeepingTask", housekeepingTask);
        return "housekeeping/my_tasks";
    }

    @GetMapping(value = "/task-detail")
    public String home(Model model, @RequestParam(name = "taskId") Long taskId) {
        HousekeepingTask task = housekeepingTaskService.getHousekeepingTaskById(taskId);
        if (task.getStatus().equals(TaskStatus.COMPLETED)) {
            boolean completed = true;
            model.addAttribute("completed", completed);
            System.out.println("completed");
        }
        model.addAttribute("housekeepingTaskDetail", task);
        return "housekeeping/task_detail";
    }
// o day dang co van de ve performance va logic chua on
    @PostMapping(value = "/update-status/completed")
    public String updateStatus(Model model, @RequestParam(name = "taskId")  Long taskId) {
        HousekeepingTask task = housekeepingTaskService.getHousekeepingTaskById(taskId);
        try {
            housekeepingTaskService.updateHousekeepingTask(taskId);

        } catch (Exception ex) {
            model.addAttribute("message", ex.getMessage());
            model.addAttribute("housekeepingTaskDetail", task);
            return "housekeeping/task_detail";
        }
        boolean completed = true;
        model.addAttribute("message", "task updated to complete");
        model.addAttribute("housekeepingTaskDetail", task);
        model.addAttribute("completed", completed);
        return "housekeeping/task_detail";
    }

    @PostMapping(value = "/add/notes")
    public String addNotes(Model model, @RequestParam(name = "taskId")  Long taskId, @RequestParam(name = "note")  String note, @RequestParam(name = "completed") boolean completed) {
        HousekeepingTask task = housekeepingTaskService.getHousekeepingTaskById(taskId);
        try {
            housekeepingTaskService.addNotesToHousekeepingTask(taskId, note);

        } catch (Exception ex) {
            model.addAttribute("message", ex.getMessage());
            model.addAttribute("housekeepingTaskDetail", task);
            model.addAttribute("completed", completed);
            return "housekeeping/task_detail";
        }
        model.addAttribute("message", "task notes added to complete");
        model.addAttribute("housekeepingTaskDetail", task);
        model.addAttribute("completed", completed);
        return  "housekeeping/task_detail";
    }

    @GetMapping(value = "/extend/booking")
    public String extendBooking(Model model, @RequestParam(name = "id")  Long id) {
        Booking booking = bookingService.findById(id);
        Scheduler lastScheduler = booking.getSchedulers()
                .get(booking.getSchedulers().size() - 1);

        Long lastTimeId = lastScheduler.getTimetable().getTimetableId();

        model.addAttribute("lastTimeId", lastTimeId);
        model.addAttribute("booking", booking);
        return "housekeeping/extend_booking";

    }

    @PostMapping(value = "/extend/booking/handle")
    public String handleExtendBooking(Model model, @RequestParam(name = "extendBookingId")  Long id, @RequestParam(name = "timeId") Long timeId, @RequestParam("price")BigDecimal price) {
        Booking booking = bookingService.findById(id);
        BigDecimal priceTotal = booking.getTotalAmount();
        BigDecimal result = priceTotal.add(price);
        booking.setTotalAmount(result);
        if (timeId == 1){
            timeId += 1L;
        } else if (timeId == 2){
            timeId += 1L;
        }  else if (timeId == 3){
            timeId += 1L;
        }  else if (timeId == 4){
            timeId = 1L;
        }
        try{
            extendBookingService.updateCheckOutTime(timeId, id);
        } catch (Exception e) {
            System.out.println("loi o day");
            model.addAttribute("message", e.getMessage());
            model.addAttribute("booking", booking);
            return "housekeeping/extend_booking";
        }
        System.out.println("extend booking");

        try {
            MomoResponseDTO responseDTO = momoPaymentService.payForExtendBooking(id, timeId);

            if (responseDTO == null) {
                System.out.println("Momo response lo roi :))");
                model.addAttribute("message", "Momo response lo roi :))");
                model.addAttribute("booking", booking);
                return "housekeeping/extend_booking";
            }

            return "redirect:" +  responseDTO.getPayUrl();
        } catch (Exception e) {
            System.out.println("Momo response lo roi :))");
            model.addAttribute("message", "Momo response lo roi :))");
            model.addAttribute("booking", booking);
            return "housekeeping/extend_booking";
        }

    }


}
