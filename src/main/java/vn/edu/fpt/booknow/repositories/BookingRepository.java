package vn.edu.fpt.booknow.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.edu.fpt.booknow.model.entities.Booking;
import vn.edu.fpt.booknow.model.entities.BookingStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

        Optional<Booking> findById(Long bookingId);

        Optional<List<Booking>> getBookingByCustomer_Email(String email);

        Optional<Booking> findByBookingCode(String bookingCode);

        List<Booking> getByBookingStatus(BookingStatus bookingStatus);

        int countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

        @Query("""
                            SELECT b FROM Booking b
                            WHERE b.checkInTime <= :end
                            AND b.checkOutTime >= :start
                        """)
        List<Booking> findBookingByDate(
                        @Param("start") LocalDateTime start,
                        @Param("end") LocalDateTime end);

        @Query("SELECT b FROM Booking b WHERE b.createdAt BETWEEN :start AND :end AND b.bookingStatus IN :statuses")
        List<Booking> findByCheckInTimeBetweenAndStatusIn(
                        @Param("start") LocalDateTime start,
                        @Param("end") LocalDateTime end,
                        @Param("statuses") List<BookingStatus> statuses);

        @Query("SELECT COUNT(b) > 0 FROM Booking b " +
                        "WHERE b.room.roomId = :roomId " +
                        "AND b.bookingStatus <> vn.edu.fpt.booknow.model.entities.BookingStatus.CANCELED " +
                        "AND b.bookingStatus <> vn.edu.fpt.booknow.model.entities.BookingStatus.FAILED " +
                        "AND b.checkInTime < :shiftEnd " + // Bắt đầu trước khi ca mới kết thúc
                        "AND b.checkOutTime > :shiftStart") // Kết thúc sau khi ca mới bắt đầu
        boolean isRoomOccupied(@Param("roomId") Long roomId,
                        @Param("shiftStart") LocalDateTime shiftStart,
                        @Param("shiftEnd") LocalDateTime shiftEnd);

        Booking getByBookingCode(String bookingCode);

        @Query("""
                        SELECT b FROM Booking b
                        JOIN FETCH b.customer
                        JOIN FETCH b.room r
                        JOIN FETCH r.roomType
                        WHERE b.bookingCode = :bookingCode
                        """)
        Optional<Booking> findByBookingCodeWithDetails(@Param("bookingCode") String bookingCode);

        @Query("""
                        SELECT b FROM Booking b
                        JOIN FETCH b.customer
                        JOIN FETCH b.room
                        ORDER BY b.bookingId desc 
                        """)
        List<Booking> findAllWithCustomer();

        List<Booking> findByBookingStatus(BookingStatus status);

        // Count all bookings except a specific status
        int countByCheckOutTimeBetweenAndBookingStatusNot(
                        LocalDateTime start,
                        LocalDateTime end,
                        BookingStatus bookingStatus);

        // Count bookings by specific status
        int countByBookingStatusAndCheckOutTimeBetween(
                        BookingStatus bookingStatus,
                        LocalDateTime start,
                        LocalDateTime end);

        int countByCheckOutTimeBetween(LocalDateTime start, LocalDateTime end);

        List<Booking> findByCheckOutTimeBetween(
                        LocalDateTime start,
                        LocalDateTime end);

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
                        @Param("end") LocalDateTime end);

        @Query("SELECT b FROM Booking b WHERE b.bookingStatus = 'PENDING_PAYMENT' AND b.createdAt < :timeLimit")
        List<Booking> findExpiredPendingBookings(@Param("timeLimit") LocalDateTime timeLimit);
}
