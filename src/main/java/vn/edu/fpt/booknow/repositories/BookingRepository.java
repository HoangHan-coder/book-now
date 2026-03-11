package vn.edu.fpt.booknow.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.fpt.booknow.model.entities.Booking;
import vn.edu.fpt.booknow.model.entities.BookingStatus;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    Optional<Booking> findByBookingCode(String bookingCode);

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
