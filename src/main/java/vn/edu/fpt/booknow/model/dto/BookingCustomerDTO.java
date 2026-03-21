package vn.edu.fpt.booknow.model.dto;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.edu.fpt.booknow.model.entities.Customer;
import vn.edu.fpt.booknow.model.entities.Room;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookingCustomerDTO {
    private Long bookingId;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;

    private Room room;

    private String checkInTime;

    private String checkOutTime;

    private String idCardFrontUrl;

    private String idCardBackUrl;

    private String bookingStatus;

    private Long totalAmount;

    private String bookingCode;

    private LocalDate createdAt;
    private String note;
}
