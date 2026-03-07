package vn.edu.fpt.booknow.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "Booking")
@Getter @Setter
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
    private LocalDateTime checkInTime;
    private LocalDateTime checkOutTime;
    private String idCardFrontUrl;
    private String idCardBackUrl;
    private String bookingStatus;
    private Double totalAmount;
    private String bookingCode;
    private LocalDateTime createdAt;


}
