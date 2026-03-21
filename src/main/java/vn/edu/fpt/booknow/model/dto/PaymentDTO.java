package vn.edu.fpt.booknow.model.dto;

import vn.edu.fpt.booknow.model.entities.Payment;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class PaymentDTO {

    private Long paymentId;

    private BookingDTO booking;

    private BigDecimal amount;

    private String method;

    private String paymentStatus;

    private LocalDateTime paidAt;

    public PaymentDTO(Payment payment) {
        this.paymentId = payment.getPaymentId();
        this.booking = new BookingDTO(payment.getBooking());
        this.amount = payment.getAmount();
        this.method = payment.getMethod();
        this.paymentStatus = payment.getPaymentStatus();
        this.paidAt = payment.getPaidAt();
    }

    public String getAmountFormated() {
        return amount != null ? NumberFormat.getNumberInstance(Locale.forLanguageTag("vi-VN")).format(amount) + " VND" : null;
    }

    public String getPaymentAtFormated() {
        return paidAt != null ? paidAt.toLocalDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))  : null;
    }

    public String getPaymentAtTime() {
        return paidAt != null ? paidAt.format(DateTimeFormatter.ofPattern("HH:mm")) : null;
    }

}
