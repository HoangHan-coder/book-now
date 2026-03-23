package vn.edu.fpt.booknow.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.fpt.booknow.model.dto.FeedbackDetailDTO;
import vn.edu.fpt.booknow.model.entities.Feedback;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {

    // UC-14.1 View Feedback List
    // Retrieve feedback list sorted by created date descending
    List<Feedback> findAllByOrderByCreatedAtDesc();

    // UC-14.2: View Feedback Detail
    @Query("SELECT f FROM Feedback f " +
            "JOIN FETCH f.booking b " +
            "JOIN FETCH b.customer c " +
            "JOIN FETCH b.room r " +
            "WHERE f.feedbackId = :feedbackId")
    Optional<Feedback> findDetailById(@Param("feedbackId") Long feedbackId);

    // UC-14.3 Hide/Show Feedback
    @Modifying
    @Query("UPDATE Feedback f SET f.isHidden = :isHidden WHERE f.feedbackId = :feedbackId")
    void updateVisibility(@Param("feedbackId") Long feedbackId,
                          @Param("isHidden") Boolean isHidden);

    Optional<Feedback> findFeedbacksByBooking_BookingId(Long bookingId);

    int countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    // UC-14.4: Update reply content of feedback
    @Modifying
    @Query("""
            UPDATE Feedback f
            SET f.contentReply = :replyContent
            WHERE f.feedbackId = :feedbackId
            """)
    void updateReply(@Param("feedbackId") Long feedbackId,
                     @Param("replyContent") String replyContent);

    @Query("""
            SELECT f FROM Feedback f
            JOIN FETCH f.booking b
            JOIN FETCH b.customer c
            JOIN FETCH b.room r
            WHERE (:rating IS NULL OR f.rating = :rating)
            AND (:hidden IS NULL OR f.isHidden = :hidden)
            ORDER BY f.createdAt DESC
            """)
    Page<Feedback> filterFeedback(Integer rating, Boolean hidden, Pageable pageable);


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
