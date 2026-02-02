package vn.edu.fpt.booknow.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.fpt.booknow.entities.Amenity;

public interface AmenityRepo extends JpaRepository<Amenity,Long> {
}
