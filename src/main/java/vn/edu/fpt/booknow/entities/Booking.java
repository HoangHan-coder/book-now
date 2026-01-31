package vn.edu.fpt.booknow.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "Booking")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "booking_id")
    private Long bookingId;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @ManyToOne
    @JoinColumn(name = "room_id")
    private Room room;

    @Column(name = "check_in_time")
    private LocalDateTime checkInTime;

    @Column(name = "check_out_date")
    private LocalDate checkOutDate;

    @Column(name = "id_card_front_url")
    private String idCardFrontUrl;

    @Column(name = "id_card_back_url")
    private String idCardBackUrl;

    @Column(name = "booking_status")
    private String bookingStatus;

    @Column(name = "total_amount")
    private Long totalAmount;

    @Column(name = "booking_code")
    private String bookingCode;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "booking")
    private List<Payment> payments;

    @OneToOne(mappedBy = "booking")
    private Invoice invoice;
}
