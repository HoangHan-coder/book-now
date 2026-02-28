package vn.edu.fpt.booknow.dto;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.edu.fpt.booknow.entities.Customer;
import vn.edu.fpt.booknow.entities.Invoice;
import vn.edu.fpt.booknow.entities.Payment;
import vn.edu.fpt.booknow.entities.Room;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookingDTO {
    private Long bookingId;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;

    private Long roomId;

    private LocalDate checkInTime;

    private LocalDate checkOutTime;

    private String idCardFrontUrl;

    private String idCardBackUrl;

    private String bookingStatus;

    private Long totalAmount;

    private String bookingCode;

    private LocalDate createdAt;
    private String note;
    private List<String> selectedSlots;
}
