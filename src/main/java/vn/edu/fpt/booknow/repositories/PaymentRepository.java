package vn.edu.fpt.booknow.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import vn.edu.fpt.booknow.model.entities.Booking;
import vn.edu.fpt.booknow.model.entities.Payment;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> getPaymentsByBooking(Booking booking);

    @Query("""
    SELECT SUM(p.amount)
    FROM Payment p
    WHERE p.paidAt BETWEEN :start AND :end
      AND p.paymentStatus = 'SUCCESS'
""")
    BigDecimal getTotalRevenue(@Param("start") LocalDateTime startTime,
                               @Param("end") LocalDateTime endTime);
}
