package vn.edu.fpt.booknow.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;
import vn.edu.fpt.booknow.model.entities.Room;
import vn.edu.fpt.booknow.model.entities.RoomStatus;

import java.util.Optional;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long>, JpaSpecificationExecutor<Room> {
    boolean existsByRoomNumber(String roomNumber);


    // tổng phòng (không tính DELETED)
    int countByStatusNot(RoomStatus status);

    @Override
    @EntityGraph(attributePaths = {
            "roomType"
    })
    Page<Room> findAll(Specification<Room> spec, Pageable pageable);

    @EntityGraph(attributePaths = {
            "roomType",
            "images",
            "roomAmenities",
            "roomAmenities.amenity"
    })
    Optional<Room> findById(Long id);

}
