package vn.edu.fpt.booknow.entities;


import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "booking_id")
    private Long bookingId;

    // ================== RELATION ==================
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    // ================== FIELDS ==================
    @Column(name = "check_in_time", nullable = false)
    private LocalDate checkInTime;

    @Column(name = "check_out_date", nullable = false)
    private LocalDate checkOutDate;

    @Column(name = "id_card_front_url", nullable = false, length = 500)
    private String idCardFrontUrl;

    @Column(name = "id_card_back_url", nullable = false, length = 500)
    private String idCardBackUrl;

    @Column(name = "booking_status", nullable = false, length = 20)
    private String bookingStatus;

    @Column(name = "total_amount", nullable = false)
    private Double totalAmount;

    @Column(name = "booking_code", length = 500)
    private String bookingCode;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
