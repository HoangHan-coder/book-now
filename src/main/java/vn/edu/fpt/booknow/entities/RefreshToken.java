package vn.edu.fpt.booknow.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "RefreshToken")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "token_hash")
    private String tokenHash;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "is_revoked")
    private Boolean isRevoked;

    @Column(name = "revoked_at")
    private LocalDateTime revokedAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "account_type")
    private String accountType; // CUSTOMER | STAFF

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @ManyToOne
    @JoinColumn(name = "staff_account_id")
    private StaffAccount staffAccount;
}
