package vn.edu.fpt.booknow.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import vn.edu.fpt.booknow.controllers.model.entities.Amenity;
import vn.edu.fpt.booknow.controllers.model.entities.Room;

import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long>, JpaSpecificationExecutor<Room> {
    boolean existsByRoomNumber(String roomNumber);

    // đếm phòng đang hoạt động
    int countByStatusIn(List<String> statuses);

    // tổng phòng (không tính DELETED)
    int countByStatusNot(String status);


}
