package vn.edu.fpt.booknow.controllers.model.entities;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "RefreshTokens")
public class RefreshTokens {

    // Primary key
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Token đã hash
    @Column(name = "token_hash", nullable = false, length = 255)
    private String tokenHash;

    // Thời gian hết hạn
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    // Token đã bị thu hồi chưa
    @Column(name = "is_revoked")
    private Boolean isRevoked;

    // Thời gian revoke
    @Column(name = "revoked_at")
    private LocalDateTime revokedAt;

    // Thời gian tạo
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // Loại account (CUSTOMER / STAFF)
    @Column(name = "account_type")
    private String accountType;

    /*
     * Many RefreshTokens -> One Customer
     */
    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;

}
