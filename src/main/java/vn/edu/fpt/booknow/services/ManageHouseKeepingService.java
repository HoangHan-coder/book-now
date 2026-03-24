package vn.edu.fpt.booknow.services;

import org.springframework.stereotype.Service;
import vn.edu.fpt.booknow.model.dto.PaginatedResponse;
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

        // ❌ VALIDATION: Cannot update staff or priority if task is already completed
        if (housekeepingTask.getStatus() == TaskStatus.COMPLETED) {
            if ((assignedStaffId != null && assignedStaffId > 0) || 
                (priority != null && !priority.isBlank())) {
                throw new IllegalStateException("Bạn không thể thay đổi nhân viên hoặc cập nhật khi trạng thái đang ở COMPLETED");
            }
        }

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
    /**
     * Get all housekeeping tasks with pagination
     * @param page Page number (1-indexed, auto-adjusted if invalid)
     * @return PaginatedResponse containing housekeeping tasks and pagination info
     */
    public PaginatedResponse<HousekeepingTask> getAllHousekeepingTaskWithPagination(int page) {
        return getAllHousekeepingTaskWithPaginationAndFilters(page, null, null);
    }

    /**
     * Get housekeeping tasks with pagination and optional filters
     * @param page Page number (1-indexed, auto-adjusted if invalid)
     * @param taskStatus Optional filter by task status
     * @param priority Optional filter by priority
     * @return PaginatedResponse containing filtered housekeeping tasks and pagination info
     */
    public PaginatedResponse<HousekeepingTask> getAllHousekeepingTaskWithPaginationAndFilters(
            int page,
            TaskStatus taskStatus,
            PriorityStatus priority) {
        final int PAGE_SIZE = 10;

        // 1️⃣ Get all tasks
        List<HousekeepingTask> allTasks = getAllHousekeepingTask();

        // 2️⃣ Apply filters
        List<HousekeepingTask> filteredTasks = allTasks.stream()
                .filter(task -> taskStatus == null || task.getStatus() == taskStatus)
                .filter(task -> priority == null || task.getPriority() == priority)
                .toList();

        // 3️⃣ Calculate pagination metrics
        long totalItems = filteredTasks.size();
        int totalPages = (int) Math.ceil((double) totalItems / PAGE_SIZE);

        // 4️⃣ Validate page number
        if (totalPages == 0) {
            totalPages = 1;
        }
        if (page < 1) {
            page = 1;
        }
        if (page > totalPages) {
            page = totalPages;
        }

        // 5️⃣ Slice data for current page
        int startIndex = (page - 1) * PAGE_SIZE;
        int endIndex = Math.min(startIndex + PAGE_SIZE, (int) totalItems);
        List<HousekeepingTask> pageData = filteredTasks.subList(startIndex, endIndex);

        // 6️⃣ Return paginated response
        return new PaginatedResponse<>(pageData, page, totalPages, totalItems, PAGE_SIZE);
    }
}
