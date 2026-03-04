package vn.edu.fpt.booknow.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.fpt.booknow.model.entities.RoomType;

public interface RoomTypeRepository extends JpaRepository<RoomType, Long> {
}
