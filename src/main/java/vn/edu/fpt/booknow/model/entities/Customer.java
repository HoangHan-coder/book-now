package vn.edu.fpt.booknow.model.entities;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table( name = "Customer")
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "customer_id")
    private Long customerId;

    // Email đăng nhập
    @Column(name = "email", nullable = false, length = 255)
    private String email;

    // Mật khẩu đã hash
    @Column(name = "password_hash", length = 255)
    private String passwordHash;

    // Họ tên khách hàng
    @Column(name = "full_name", length = 50)
    private String fullName;

    // Avatar URL
    @Column(name = "avatar_url", length = 255)
    private String avatarUrl;

    // Số điện thoại
    @Column(name = "phone", length = 20)
    private String phone;

    // Trạng thái tài khoản
    @Column(name = "status", length = 50)
    private String status;

    // Thời gian tạo
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // Thời gian cập nhật
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Soft delete
    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted;

    // ID avatar trên cloud (Cloudinary)
    @Column(name = "avatar_public_id", length = 255)
    private String avatarPublicId;

    /*
     * Relationship
     * Customer 1 - N Booking
     */
    @OneToMany(mappedBy = "customer")
    private List<Booking> bookings;

    /*
     * Customer 1 - N RefreshTokens
     */
    @OneToMany(mappedBy = "customer")
    private List<RefreshTokens> refreshTokens;

    // Constructor rỗng
    public Customer() {
    }

    // Getter & Setter

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Boolean getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(Boolean deleted) {
        isDeleted = deleted;
    }

    public String getAvatarPublicId() {
        return avatarPublicId;
    }

    public void setAvatarPublicId(String avatarPublicId) {
        this.avatarPublicId = avatarPublicId;
    }

    public List<Booking> getBookings() {
        return bookings;
    }

    public void setBookings(List<Booking> bookings) {
        this.bookings = bookings;
    }

    public List<RefreshTokens> getRefreshTokens() {
        return refreshTokens;
    }

    public void setRefreshTokens(List<RefreshTokens> refreshTokens) {
        this.refreshTokens = refreshTokens;
    }
}
