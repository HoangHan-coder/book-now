package vn.edu.fpt.booknow.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long customerId;

    private String email;

    private String passwordHash;

    private String fullName;

    private String avatarUrl;

    private String phone;

    private String status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private Boolean isDeleted;

    @OneToMany(mappedBy = "customer")
    private List<Booking> bookings;
}
