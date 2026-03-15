package vn.edu.fpt.booknow.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.fpt.booknow.model.entities.Booking;
import vn.edu.fpt.booknow.model.entities.Payment;
import vn.edu.fpt.booknow.repositories.PaymentRepository;

@Service
public class PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Transactional
    public Payment getPaymentByBookingId(Booking booking) {
        return paymentRepository.getPaymentsByBooking(booking).getLast();
    }

    @Transactional
    public void creatPayment(Payment payment) {
        paymentRepository.save(payment);
    }


}
