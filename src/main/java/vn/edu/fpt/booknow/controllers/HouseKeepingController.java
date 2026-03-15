package vn.edu.fpt.booknow.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import vn.edu.fpt.booknow.model.entities.ApproveRequest;
import vn.edu.fpt.booknow.model.entities.HousekeepingTask;
import vn.edu.fpt.booknow.repositories.HousekeepingTaskRepository;
import vn.edu.fpt.booknow.services.HousekeepingTaskService;

import java.util.List;

@Controller
@RequestMapping(value = "/housekeeping")
public class HouseKeepingController {
    @Autowired
    private HousekeepingTaskService housekeepingTaskService;

    @GetMapping(value = "/task")
    public String task(Model model) {
        List<HousekeepingTask> housekeepingTask = housekeepingTaskService.getAllHousekeepingTask();
        model.addAttribute("housekeepingTask", housekeepingTask);
        return "housekeeping/my_tasks";
    }

    @GetMapping(value = "/task-detail")
    public String home(Model model, @RequestParam(name = "taskId") Long taskId) {
        HousekeepingTask task = housekeepingTaskService.getHousekeepingTaskById(taskId);
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

}
