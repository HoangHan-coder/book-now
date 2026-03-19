package vn.edu.fpt.booknow.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.fpt.booknow.model.dto.FeedbackDetailDTO;
import vn.edu.fpt.booknow.model.entities.Feedback;

import java.util.List;
import java.util.Optional;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {

    Optional<Feedback> findFeedbacksByBooking_BookingId(Long bookingId);

    @Query("""
    SELECT new vn.edu.fpt.booknow.model.dto.FeedbackDetailDTO(
        c.fullName, 
        c.avatarUrl, 
        f.rating, 
        f.content, 
        f.createdAt,
        CAST((SELECT COUNT(b2) FROM Booking b2 WHERE b2.customer.customerId = c.customerId) AS long),
        f.contentReply, 
        f.createdAt, 
        s.fullName
    )
    FROM Feedback f
    JOIN Booking b ON f.booking.bookingId = b.bookingId
    JOIN Customer c ON b.customer.customerId = c.customerId
    LEFT JOIN StaffAccount s ON f.admin.staffAccountId = s.staffAccountId
    WHERE b.room.roomId = :roomId 
      AND f.isHidden = false
    ORDER BY f.createdAt DESC
""")
    List<FeedbackDetailDTO> findFeedbacksByRoomId(@Param("roomId") Long roomId);
}
