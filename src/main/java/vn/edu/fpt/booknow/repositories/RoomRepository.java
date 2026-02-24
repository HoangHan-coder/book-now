package vn.edu.fpt.booknow.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
     Page<Room> findByRoomNumberContaining(
             String roomNumber,
             Pageable pageable
     );

    Page<Room> findByStatusAndRoomType_Name(
            String status,
            String type,
            Pageable pageable
    );

    Page<Room> findByStatus(
            String status,
            Pageable pageable
    );

    Page<Room> findByRoomType_Name(
            String type,
            Pageable pageable
    );
}
