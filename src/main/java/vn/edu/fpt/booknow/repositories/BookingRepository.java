package vn.edu.fpt.booknow.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.edu.fpt.booknow.model.entities.Booking;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

   Optional<List<Booking>> getBookingByCustomer_Email(String email);

   Optional<Booking> findByBookingCode(String bookingCode);

   List<Booking> getByBookingStatus(String bookingStatus);

   @Query("SELECT COUNT(b) > 0 FROM Booking b " +
         "WHERE b.room.roomId = :roomId " +
         "AND b.bookingStatus <> 'CANCELLED' " +
         "AND b.checkInTime > :shiftStart " +
         "AND b.checkOutTime < :shiftEnd")
   boolean isRoomOccupied(@Param("roomId") Long roomId,
         @Param("shiftStart") LocalDateTime shiftStart,
         @Param("shiftEnd") LocalDateTime shiftEnd);
}
