package vn.edu.fpt.booknow.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.fpt.booknow.model.entities.RoomType;

@Repository
public interface RoomTypeRepository extends JpaRepository<RoomType, Long> {

}
