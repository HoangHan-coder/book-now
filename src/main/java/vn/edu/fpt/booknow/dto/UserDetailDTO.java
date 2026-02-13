package vn.edu.fpt.booknow.dto;

import java.time.LocalDateTime;

// UC-17.2: View User Detail
public class UserDetailDTO {

    private String userId;
    private String fullName;
    private String email;
    private String phone;
    private String role;
    private String avatarUrl;
    private String status;
    private LocalDateTime createdAt;

    public UserDetailDTO(String userId, String fullName, String email,
                         String phone, String role,
                         String avatarUrl, String status,
                         LocalDateTime createdAt) {
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.role = role;
        this.avatarUrl = avatarUrl;
        this.status = status;
        this.createdAt = createdAt;
    }

    public String getUserId() { return userId; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getRole() { return role; }
    public String getAvatarUrl() { return avatarUrl; }
    public String getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
