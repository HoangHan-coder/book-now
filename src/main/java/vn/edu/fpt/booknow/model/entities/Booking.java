package vn.edu.fpt.booknow.model.entities;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Nationalized;


import java.math.BigDecimal;
import java.time.Instant;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Getter
@Setter
@Entity
@Table(name = "Booking", schema = "dbo", indexes = {
        @Index(name = "IX_Booking_Room_Status", columnList = "room_id, booking_status"),
        @Index(name = "IX_Booking_Customer_CreatedAt", columnList = "customer_id, created_at"),
        @Index(name = "IX_Booking_CheckedIn_CheckoutTime", columnList = "booking_status, check_out_time")
})
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "booking_id", nullable = false)
    private Long bookingId;


    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;


    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;


    @NotNull
    @Column(name = "check_in_time", nullable = false)
    private LocalDateTime checkInTime;


    @NotNull
    @Column(name = "check_out_time", nullable = false)
    private LocalDateTime checkOutTime;

    @Column(name = "actual_check_in_time")
    private LocalDateTime actualCheckInTime;

    @Column(name = "actual_check_out_time")
    private LocalDateTime actualCheckOutTime;

    @Size(max = 500)
    @NotNull
    @Nationalized
    @Column(name = "id_card_front_url", nullable = false, length = 500)
    private String idCardFrontUrl;


    @Size(max = 500)
    @NotNull
    @Nationalized
    @Column(name = "id_card_back_url", nullable = false, length = 500)
    private String idCardBackUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "booking_status", nullable = false, length = 20)
    private BookingStatus bookingStatus;

    @NotNull
    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;


    @Size(max = 500)
    @Nationalized
    @Column(name = "booking_code", length = 500)
    private String bookingCode;


    @NotNull
    @ColumnDefault("sysdatetime()")
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Size(max = 255)
    @Nationalized
    @Column(name = "note")
    private String note;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Size(max = 255)
    @Column(name = "id_card_font_public_id")
    private String idCardFontPublicId;

    @Size(max = 255)
    @Column(name = "id_card_back_public_id")
    private String idCardBackPublicId;

    @OneToMany(mappedBy = "booking")
    private List<Scheduler> schedulers = new ArrayList<>();

    @OneToMany(mappedBy = "booking")
    private List<Feedback> feedbacks = new ArrayList<>();

    @OneToMany(mappedBy = "booking")
    private List<vn.edu.fpt.booknow.model.entities.Invoice> invoices = new ArrayList<>();

    @OneToMany(mappedBy = "booking")
    private List<vn.edu.fpt.booknow.model.entities.Payment> payments = new ArrayList<>();

    @OneToOne(mappedBy = "booking")
    private CheckInSession checkInSession;

    @OneToOne(mappedBy = "booking")
    private HousekeepingTask housekeepingTasks;

}
