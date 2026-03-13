package vn.edu.fpt.booknow.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.fpt.booknow.controllers.model.entities.Image;

public interface ImageRepository extends JpaRepository<Image, Long> {
}
