package vn.edu.fpt.booknow.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import vn.edu.fpt.booknow.model.entities.CheckInSession;

import java.util.Optional;

@Repository
public interface CheckInSessionRepository extends JpaRepository<CheckInSession,Long> {
    Optional<CheckInSession> findCheckInSessionByBooking_BookingId(Long bookingId);
}
