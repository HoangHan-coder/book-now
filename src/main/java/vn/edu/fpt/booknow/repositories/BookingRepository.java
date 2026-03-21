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

   Optional<List<Booking>> getBookingByCustomer_Email(String email);

   Optional<Booking> findByBookingCode(String bookingCode);


    @Query("SELECT COUNT(b) > 0 FROM Booking b " +
            "WHERE b.room.roomId = :roomId " +
            "AND b.bookingStatus <> vn.edu.fpt.booknow.model.entities.BookingStatus.CANCELLED " +
            "AND b.bookingStatus <> vn.edu.fpt.booknow.model.entities.BookingStatus.FAILED " +
            "AND b.checkInTime < :shiftEnd " +   // Bắt đầu trước khi ca mới kết thúc
            "AND b.checkOutTime > :shiftStart")  // Kết thúc sau khi ca mới bắt đầu
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
       """)
    List<Booking> findAllWithCustomer();
    List<Booking> findByBookingStatus(BookingStatus status);

}
