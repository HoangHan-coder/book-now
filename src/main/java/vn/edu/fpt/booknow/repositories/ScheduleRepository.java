package vn.edu.fpt.booknow.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.fpt.booknow.model.entities.Scheduler;

public interface ScheduleRepository extends JpaRepository<Scheduler, Long> {
}
