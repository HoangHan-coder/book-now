package vn.edu.fpt.booknow.controllers.staff;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.edu.fpt.booknow.model.dto.PaginatedResponse;
import vn.edu.fpt.booknow.model.entities.HousekeepingTask;
import vn.edu.fpt.booknow.model.entities.RoomStatus;
import vn.edu.fpt.booknow.model.entities.TaskStatus;
import vn.edu.fpt.booknow.model.entities.PriorityStatus;
import vn.edu.fpt.booknow.repositories.StaffAccountRepository;
import vn.edu.fpt.booknow.services.ManageHouseKeepingService;

import java.util.List;

@Controller
@RequestMapping("/staff")
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
            @RequestParam(name = "taskStatus", required = false) String taskStatusStr,
            @RequestParam(name = "priority", required = false) String priorityStr,
            @RequestParam(name = "page", defaultValue = "1") int page,
            Model model) {

        // Parse filter parameters
        TaskStatus taskStatus = null;
        PriorityStatus priority = null;

        if (taskStatusStr != null && !taskStatusStr.isBlank()) {
            try {
                taskStatus = TaskStatus.valueOf(taskStatusStr);
            } catch (IllegalArgumentException e) {
                // Invalid enum value, ignore filter
            }
        }

        if (priorityStr != null && !priorityStr.isBlank()) {
            try {
                priority = PriorityStatus.valueOf(priorityStr);
            } catch (IllegalArgumentException e) {
                // Invalid enum value, ignore filter
            }
        }

        // Get filtered and paginated tasks
        PaginatedResponse<HousekeepingTask> paginatedTasks = 
                manageHouseKeepingService.getAllHousekeepingTaskWithPaginationAndFilters(page, taskStatus, priority);
        
        model.addAttribute("housekeepingTaskLists", paginatedTasks.getData());
        model.addAttribute("currentPage", paginatedTasks.getCurrentPage());
        model.addAttribute("totalPages", paginatedTasks.getTotalPages());
        model.addAttribute("totalItems", paginatedTasks.getTotalItems());
        model.addAttribute("hasNext", paginatedTasks.isHasNext());
        model.addAttribute("hasPrevious", paginatedTasks.isHasPrevious());
        
        // Pass filter values back to template
        model.addAttribute("selectedTaskStatus", taskStatus);
        model.addAttribute("selectedPriority", priority);
        

        // Add enums to model for template
        model.addAttribute("taskStatuses", TaskStatus.values());
        model.addAttribute("priorities", PriorityStatus.values());
        
        return "private/staff-manage-house-keeping";
    }

    @GetMapping("/manage-housekeeping/task-detail/{id}")
    public String showHouseKeepingTaskDetail(
            @PathVariable("id") Long id,
            Model model) {
        System.out.println(id);
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
            RedirectAttributes redirectAttributes) {
        try {
            HousekeepingTask housekeepingTask = manageHouseKeepingService.updateHousekeepingTaskDetail(id, assignedStaffId, priority, notes);
            redirectAttributes.addFlashAttribute("successMessage", "Đã giao nhiệm vụ thành công");
            return "redirect:/staff/manage-housekeeping";
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage",  e.getMessage());
            return "redirect:/staff/manage-housekeeping";
        }
    }

}
