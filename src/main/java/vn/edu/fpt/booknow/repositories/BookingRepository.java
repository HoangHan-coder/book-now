package vn.edu.fpt.booknow.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.fpt.booknow.model.entities.Booking;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    // Count all bookings except a specific status
    int countByCheckOutTimeBetweenAndBookingStatusNot(
            LocalDateTime start,
            LocalDateTime end,
            String bookingStatus
    );

    // Count bookings by specific status
    int countByBookingStatusAndCheckOutTimeBetween(
            String bookingStatus,
            LocalDateTime start,
            LocalDateTime end
    );

    int countByCheckOutTimeBetween(LocalDateTime start, LocalDateTime end);

    List<Booking> findByCheckOutTimeBetween(
            LocalDateTime start,
            LocalDateTime end
    );

    // Count bookings grouped by status
    @Query("""
                SELECT b.bookingStatus, COUNT(b)
                FROM Booking b
                WHERE b.checkOutTime BETWEEN :start AND :end
                GROUP BY b.bookingStatus
            """)
    List<Object[]> countByStatus(LocalDateTime start, LocalDateTime end);

    // ===============================
    // Doanh thu
    // ===============================

    // Tổng doanh thu chỉ cho booking trạng thái COMPLETED
    @Query("""
                SELECT COALESCE(SUM(b.totalAmount), 0)
                FROM Booking b
                WHERE b.checkOutTime BETWEEN :start AND :end
                  AND b.bookingStatus = 'COMPLETED'
            """)
    long sumRevenueCompleted(LocalDateTime start, LocalDateTime end);



    // Doanh thu theo ngày chỉ cho COMPLETED
    @Query("""
                SELECT CAST(b.checkOutTime AS date), SUM(b.totalAmount)
                FROM Booking b
                WHERE b.checkOutTime BETWEEN :start AND :end
                  AND b.bookingStatus = 'COMPLETED'
                GROUP BY CAST(b.checkOutTime AS date)
            """)
    List<Object[]> revenueByDateCompleted(LocalDateTime start, LocalDateTime end);

    @Query("""
                SELECT COUNT(b)
                FROM Booking b
                WHERE b.checkOutTime BETWEEN :start AND :end
            """)
    int countAllByCheckOutTime(LocalDateTime start, LocalDateTime end);

    @Query("""
    SELECT COUNT(DISTINCT b.room.roomId)
    FROM Booking b
    WHERE b.bookingStatus IN ('COMPLETED', 'PAID')
      AND b.checkInTime <= :end
      AND b.checkOutTime >= :start
""")
    long countActiveRooms(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );
}