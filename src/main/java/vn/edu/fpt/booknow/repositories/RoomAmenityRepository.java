package vn.edu.fpt.booknow.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.fpt.booknow.entities.Room;
import vn.edu.fpt.booknow.entities.RoomAmenity;

import java.util.List;

public interface RoomAmenityRepository extends JpaRepository<RoomAmenity, Long> {
    List<RoomAmenity> findByRoom(Room room);
    void deleteByRoom(Room room);
}
