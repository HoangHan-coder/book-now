package vn.edu.fpt.booknow.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.edu.fpt.booknow.controllers.model.entities.Booking;


import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    int countByCreatedAtBetweenAndBookingStatusNot(
            LocalDateTime start,
            LocalDateTime end,
            String bookingStatus
    );

    List<Booking> findByCheckOutTimeBetween(
            LocalDateTime start,
            LocalDateTime end
    );

    int countByBookingStatusAndCreatedAtBetween(
            String bookingStatus,
            LocalDateTime start,
            LocalDateTime end
    );
    int countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

        @Query("""
    SELECT b.bookingStatus, COUNT(b)
    FROM Booking b
    WHERE b.createdAt BETWEEN :start AND :end
    GROUP BY b.bookingStatus
    """)
        List<Object[]> countByStatus(LocalDateTime start, LocalDateTime end);

}

