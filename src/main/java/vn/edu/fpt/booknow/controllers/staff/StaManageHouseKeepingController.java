package vn.edu.fpt.booknow.controllers.staff;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import vn.edu.fpt.booknow.model.entities.HousekeepingTask;
import vn.edu.fpt.booknow.model.entities.RoomStatus;
import vn.edu.fpt.booknow.repositories.StaffAccountRepository;
import vn.edu.fpt.booknow.services.staff.ManageHouseKeepingService;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class StaManageHouseKeepingController {
    private final ManageHouseKeepingService manageHouseKeepingService;
    private final StaffAccountRepository staffAccountRepository;

    public StaManageHouseKeepingController(ManageHouseKeepingService manageHouseKeepingService,
                                         StaffAccountRepository staffAccountRepository) {
        this.manageHouseKeepingService = manageHouseKeepingService;
        this.staffAccountRepository = staffAccountRepository;
    }

    @GetMapping("/manage-housekeeping")
    public String showManageHouseKeepingPage(
            @RequestParam(name = "roomStatus", required = false) String roomStatusStr,
            Model model) {

        List<HousekeepingTask> allTasks = manageHouseKeepingService.getAllHousekeepingTask();
        
        model.addAttribute("housekeepingTaskLists", allTasks);
        return "private/staff-manage-house-keeping";
    }

    @GetMapping("/manage-housekeeping/task-detail/{id}")
    public String showHouseKeepingTaskDetail(
            @PathVariable("id") Long id,
            Model model) {
        HousekeepingTask housekeepingTask = manageHouseKeepingService.getHousekeepingTaskById(id);
        model.addAttribute("housekeepingTask", housekeepingTask);
        model.addAttribute("availableStaff", staffAccountRepository.findAll());
        return "private/staff-housekeeping-task-detail";
    }

    @PostMapping("/manage-housekeeping/task-detail/{id}")
    public String updateHouseKeepingTaskDetail(
            @PathVariable("id") Long id,
            @RequestParam(value = "assignedStaffId", required = false) Long assignedStaffId,
            @RequestParam(value = "priority", required = false) String priority,
            @RequestParam(value = "notes", required = false) String notes,
            Model model) {
        HousekeepingTask housekeepingTask = manageHouseKeepingService.updateHousekeepingTaskDetail(id, assignedStaffId, priority, notes);
        model.addAttribute("housekeepingTask", housekeepingTask);
        model.addAttribute("availableStaff", staffAccountRepository.findAll());
        model.addAttribute("successMessage", "Task updated successfully!");
        return "redirect:/admin/manage-housekeeping";
    }

}
