package vn.edu.fpt.booknow.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.fpt.booknow.model.entities.Booking;
import vn.edu.fpt.booknow.model.entities.Payment;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> getPaymentsByBooking(Booking booking);
}
