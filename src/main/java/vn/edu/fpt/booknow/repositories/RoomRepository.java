package vn.edu.fpt.booknow.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.fpt.booknow.entities.Room;

import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {
    @Query("""
    SELECT r FROM Room r
    JOIN FETCH r.roomType rt
    WHERE (:status IS NULL OR r.status = :status)
      AND (:type IS NULL OR rt.typeCode = :type)
      AND (:roomNumber IS NULL OR r.roomNumber LIKE :roomNumber)
""")
    List<Room> search(
            @Param("status") String status,
            @Param("type") String type,
            @Param("roomNumber") String roomNumber
    );

}
