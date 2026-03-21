package vn.edu.fpt.booknow.model.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Nationalized;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "HousekeepingTask", schema = "dbo", indexes = {
        @Index(name = "IX_HousekeepingTask_Status_Type", columnList = "task_status, task_type"),
        @Index(name = "IX_HousekeepingTask_AssignedTo", columnList = "assigned_to, task_status")
})
public class HousekeepingTask {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "task_id", nullable = false)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id")
    private Booking booking;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to")
    private StaffAccount assignedTo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private StaffAccount createdBy;

    @Size(max = 30)
    @NotNull
    @Column(name = "task_type", nullable = false, length = 30)
    private String taskType;

    @Enumerated(EnumType.STRING)
    @NotNull
    @ColumnDefault("'PENDING'")
    @Column(name = "task_status", nullable = false, length = 20)
    private TaskStatus status;

    @Enumerated(EnumType.STRING)
    @NotNull
    @ColumnDefault("'NORMAL'")
    @Column(name = "priority", nullable = false, length = 10)
    private PriorityStatus priority;

    @Size(max = 1000)
    @Nationalized
    @Column(name = "notes", length = 1000)
    private String notes;

    @NotNull
    @ColumnDefault("sysdatetime()")
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

}