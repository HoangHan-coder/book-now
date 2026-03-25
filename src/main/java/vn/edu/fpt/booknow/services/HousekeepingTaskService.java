package vn.edu.fpt.booknow.services;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vn.edu.fpt.booknow.model.entities.Booking;
import vn.edu.fpt.booknow.model.entities.HousekeepingTask;
import vn.edu.fpt.booknow.model.entities.Room;
import vn.edu.fpt.booknow.model.entities.TaskStatus;
import vn.edu.fpt.booknow.repositories.HousekeepingTaskRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class HousekeepingTaskService {
    @Autowired
    private HousekeepingTaskRepository housekeepingTaskRepository;
    public List<HousekeepingTask> getAllHousekeepingTask() {
        return housekeepingTaskRepository.findAll();
    }

    public HousekeepingTask getHousekeepingTaskById(Long id) {
        return housekeepingTaskRepository.findById(id).orElse(null);
    }

    @Transactional
    public HousekeepingTask getByBooKingCode(String bookingCode){
        return housekeepingTaskRepository.getHousekeepingTaskByBooking_BookingCode(bookingCode);
    }

    @Transactional
    public void updateHousekeepingTask(Long id) throws Exception {
        HousekeepingTask housekeepingTask = housekeepingTaskRepository.findById(id).orElse(null);
        if (housekeepingTask != null) {
            LocalDateTime now = LocalDateTime.now();
            housekeepingTask.setStatus(TaskStatus.COMPLETED);
            housekeepingTask.setCompletedAt(now);
        } else {
            throw new RuntimeException("HousekeepingTask not found");
        }
        housekeepingTaskRepository.save(housekeepingTask);
    }

    @Transactional
    public void addNotesToHousekeepingTask(Long id, String notes) throws Exception {
        HousekeepingTask task = housekeepingTaskRepository.findById(id).orElse(null);
        if (task != null) {
        task.setNoteHousekeeping(notes);
        } else  {
            throw new RuntimeException("HousekeepingTask not found");
        }
        housekeepingTaskRepository.save(task);
    }

    @Transactional
    public HousekeepingTask newTask(HousekeepingTask housekeepingTask) {
        return housekeepingTaskRepository.save(housekeepingTask);
    }

}
