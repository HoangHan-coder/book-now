package vn.edu.fpt.booknow.model.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "booking_id", nullable = false)
    private Long bookingId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Column(name = "check_in_time", nullable = false)
    private LocalDateTime checkInTime;

    @Column(name = "check_out_time", nullable = false)
    private LocalDateTime checkOutTime;


    @Column(name = "id_card_front_url", nullable = false, length = 500)
    private String idCardFrontUrl;


    @Column(name = "id_card_back_url", nullable = false, length = 500)
    private String idCardBackUrl;


    @Enumerated(EnumType.STRING)
    @Column(name = "booking_status", nullable = false, length = 20)
    private BookingStatus bookingStatus;

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;


    @Column(name = "booking_code", length = 500)
    private String bookingCode;

    @ColumnDefault("sysdatetime()")
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "booking")
    private List<Feedback> feedbacks = new ArrayList<>();

    @OneToMany(mappedBy = "booking")
    private List<Invoice> invoices = new ArrayList<>();

    @OneToMany(mappedBy = "booking")
    private List<Payment> payments = new ArrayList<>();

    @OneToMany(mappedBy = "booking")
    private List<Scheduler> schedulers = new ArrayList<>();

    @Column(name = "note")
    private String note;
    @Column(name = "update_at")
    private LocalDateTime updateAt;
    @Column(name = "actual_check_in_time")
    private Instant actualCheckInTime;
    @Column(name = "actual_check_out_time")
    private Instant actualCheckOutTime;

}