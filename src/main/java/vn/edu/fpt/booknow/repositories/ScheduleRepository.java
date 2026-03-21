package vn.edu.fpt.booknow.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.edu.fpt.booknow.model.entities.Scheduler;

import java.time.LocalDateTime;

public interface ScheduleRepository extends JpaRepository<Scheduler, Long> {

}
