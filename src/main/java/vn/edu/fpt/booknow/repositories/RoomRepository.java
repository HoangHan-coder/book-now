package vn.edu.fpt.booknow.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.fpt.booknow.entities.Room;

import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {
     //List<Room> searchRoom(String status, Long roomTypeId, String roomNumber);
     //List<String> findDistinctStatus();
}
