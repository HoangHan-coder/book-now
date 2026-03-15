package vn.edu.fpt.booknow.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.edu.fpt.booknow.controllers.model.entities.Amenity;
import vn.edu.fpt.booknow.controllers.model.entities.Room;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long>, JpaSpecificationExecutor<Room> {
    boolean existsByRoomNumber(String roomNumber);

    // đếm phòng đang hoạt động
    int countByStatusIn(List<String> statuses);

    // tổng phòng (không tính DELETED)
    int countByStatusNot(String status);

    @Override
    @EntityGraph(attributePaths = {"roomType"})
    Page<Room> findAll(org.springframework.data.jpa.domain.Specification<Room> spec, Pageable pageable);

}
