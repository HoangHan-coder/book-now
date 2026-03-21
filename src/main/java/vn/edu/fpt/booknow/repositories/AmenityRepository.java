package vn.edu.fpt.booknow.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.fpt.booknow.model.entities.Amenity;

public interface AmenityRepository extends JpaRepository<Amenity, Long> {
    Amenity findByNameIgnoreCase(String name);
}
