package vn.edu.fpt.booknow.model.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Nationalized;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "StaffAccounts", schema = "dbo", uniqueConstraints = {
        @UniqueConstraint(name = "UQ_Admin_Email", columnNames = {"email"})
})
public class StaffAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "staff_account_id", nullable = false)
    private Long staffAccountId;

    @Size(max = 255)
    @NotNull
    @Nationalized
    @Column(name = "email", nullable = false)
    private String email;

    @Size(max = 20)
    @Nationalized
    @Column(name = "phone", length = 20)
    private String phone;

    @Size(max = 255)
    @NotNull
    @Nationalized
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Size(max = 50)
    @Nationalized
    @Column(name = "full_name", length = 50)
    private String fullName;

    @Size(max = 255)
    @Nationalized
    @Column(name = "avatar_url",columnDefinition = "nvarchar(255)")
    private String avatarUrl;

    @Nationalized
    @Column(name = "role", nullable = false, length = 20)
    private String role;

    @Size(max = 50)
    @NotNull
    @ColumnDefault("'ACTIVE'")
    @Column(name = "status", nullable = false, length = 50)
    private String status;

    @NotNull
    @ColumnDefault("sysdatetime()")
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @NotNull
    @ColumnDefault("0")
    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @Size(max = 255)
    @Nationalized
    @Column(name = "avatar_public_id")
    private String avatarPublicId;
    @OneToMany(mappedBy = "admin")

    private List<Feedback> feedbacks = new ArrayList<>();

    @OneToMany(mappedBy = "staffAccount")
    private List<RefreshToken> refreshTokens = new ArrayList<>();

    @OneToMany(mappedBy = "assignedTo")
    private List<HousekeepingTask> housekeepingTasksAssignedTo = new ArrayList<>();

    @OneToMany(mappedBy = "createdBy")
    private List<HousekeepingTask> housekeepingTasksCreateBy = new ArrayList<>();

    @OneToMany(mappedBy = "staffAccount")
    private List<CheckInSession> checkInSession;

}