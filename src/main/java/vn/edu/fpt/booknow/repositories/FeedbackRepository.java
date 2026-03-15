package vn.edu.fpt.booknow.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.fpt.booknow.model.entities.Feedback;

import java.util.Optional;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    Optional<Feedback> findFeedbacksByBooking_BookingId(Long bookingId);
}
