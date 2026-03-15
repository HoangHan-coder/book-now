package vn.edu.fpt.booknow.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.edu.fpt.booknow.model.entities.HousekeepingTask;
import vn.edu.fpt.booknow.model.entities.RoomStatus;
import vn.edu.fpt.booknow.model.entities.TaskStatus;

import java.util.List;


@Repository
public interface HouseKeepingRepository extends JpaRepository<HousekeepingTask, Long> {

	@Query("""
    SELECT DISTINCT t
    FROM HousekeepingTask t
    LEFT JOIN FETCH t.room r
    LEFT JOIN FETCH r.roomType
    LEFT JOIN FETCH t.booking
    LEFT JOIN FETCH t.assignedTo
    """)
	List<HousekeepingTask> findAllWithDetails();




}
