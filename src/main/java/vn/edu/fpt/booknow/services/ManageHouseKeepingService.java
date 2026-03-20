package vn.edu.fpt.booknow.services;

import org.springframework.stereotype.Service;
import vn.edu.fpt.booknow.model.entities.HousekeepingTask;
import vn.edu.fpt.booknow.model.entities.PriorityStatus;
import vn.edu.fpt.booknow.model.entities.TaskStatus;
import vn.edu.fpt.booknow.model.entities.StaffAccount;
import vn.edu.fpt.booknow.repositories.HouseKeepingRepository;
import vn.edu.fpt.booknow.repositories.StaffAccountRepository;

import java.util.List;
import java.util.Optional;


@Service
public class ManageHouseKeepingService {
    private final HouseKeepingRepository houseKeepingRepository;
    private final StaffAccountRepository staffAccountRepository;

    public ManageHouseKeepingService(HouseKeepingRepository houseKeepingRepository,
                                   StaffAccountRepository staffAccountRepository) {
        this.houseKeepingRepository = houseKeepingRepository;
        this.staffAccountRepository = staffAccountRepository;
    }

    public List<HousekeepingTask> getAllHousekeepingTask() {
        List<HousekeepingTask> housekeepingTasks = houseKeepingRepository.findAllWithDetails();
        if (housekeepingTasks.isEmpty()) {
            throw new IllegalStateException("No housekeeping tasks found");
        }
        return housekeepingTasks;
    }

    public HousekeepingTask getHousekeepingTaskById(Long id) {
        HousekeepingTask housekeepingTask = houseKeepingRepository.findById(id).orElseThrow();
        return housekeepingTask;
    }

    public HousekeepingTask updateHousekeepingTaskDetail(
            Long taskId,
            Long assignedStaffId,
            String priority,
            String notes) {

        HousekeepingTask housekeepingTask = houseKeepingRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found with ID: " + taskId));

        // Assign staff and auto-update status
        if (assignedStaffId != null && assignedStaffId > 0) {
            Optional<StaffAccount> staff = staffAccountRepository.findById(assignedStaffId);
            if (staff.isPresent()) {
                housekeepingTask.setAssignedTo(staff.get());
                
                // AUTO STATUS: PENDING -> ASSIGNED when staff is assigned
                if (housekeepingTask.getStatus() == TaskStatus.PENDING) {
                    housekeepingTask.setStatus(TaskStatus.ASSIGNED);
                }
            }
        } else {
            housekeepingTask.setAssignedTo(null);
        }

        // Update priority
        if (priority != null && !priority.isBlank()) {
            housekeepingTask.setPriority(PriorityStatus.valueOf(priority));
        }

        // Update notes
        if (notes != null && !notes.isBlank()) {
            housekeepingTask.setNotes(notes);
        }

        return houseKeepingRepository.save(housekeepingTask);
    }

}
