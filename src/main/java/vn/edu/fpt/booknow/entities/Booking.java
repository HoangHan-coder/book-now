package vn.edu.fpt.booknow.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "Booking")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long bookingId;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @ManyToOne
    @JoinColumn(name = "room_id")
    private Room room;

    private LocalDate checkInTime;

    private LocalDate checkOutDate;

    private String idCardFrontUrl;

    private String idCardBackUrl;

    private String bookingStatus;

    private Long totalAmount;

    private String bookingCode;

    private LocalDate createdAt;

    @OneToMany(mappedBy = "booking")
    private List<Payment> payments;

    @OneToOne(mappedBy = "booking")
    private Invoice invoice;
}
