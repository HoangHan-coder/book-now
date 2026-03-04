package vn.edu.fpt.booknow.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.fpt.booknow.model.entities.Booking;

public interface BookingRepository extends JpaRepository<Booking,Long> {
}
