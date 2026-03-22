package vn.edu.fpt.booknow.model.entities;


import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Nationalized;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "staffaccounts")
public class StaffAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "staff_account_id", nullable = false)
    private Long staffAccountId;

    @Nationalized
    @Column(name = "email", nullable = false)
    private String email;

    @Nationalized
    @Column(name = "phone", length = 20)
    private String phone;

    @Nationalized
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Nationalized
    @Column(name = "full_name", length = 50)
    private String fullName;

    @Nationalized
    @Column(name = "avatar_url")
    private String avatarUrl;

    @Nationalized
    @Column(name = "role", nullable = false, length = 20)
    private String role;

    @ColumnDefault("'ACTIVE'")
    @Column(name = "status", nullable = false, length = 50)
    private String status;

    @ColumnDefault("sysdatetime()")
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @ColumnDefault("0")
    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @OneToMany(mappedBy = "admin")
    private List<vn.edu.fpt.booknow.model.entities.Feedback> feedbacks = new ArrayList<>();

    @OneToMany(mappedBy = "staffAccount")
    private List<RefreshToken> refreshTokens = new ArrayList<>();

    @OneToMany(mappedBy = "assignedTo")
    private List<HousekeepingTask> housekeepingTasksAssignedTo = new ArrayList<>();

    @OneToMany(mappedBy = "createdBy")
    private List<HousekeepingTask> housekeepingTasksCreateBy = new ArrayList<>();

    @OneToMany(mappedBy = "staffAccount")
    private List<CheckInSession> checkInSession;

}