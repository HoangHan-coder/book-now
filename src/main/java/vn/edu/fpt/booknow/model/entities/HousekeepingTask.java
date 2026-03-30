package vn.edu.fpt.booknow.model.entities;

import jakarta.persistence.Entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Nationalized;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "HousekeepingTask", schema = "dbo", indexes = {
        @Index(name = "IX_HousekeepingTask_Status_Type", columnList = "task_status, task_type"),
        @Index(name = "IX_HousekeepingTask_AssignedTo", columnList = "assigned_to, task_status")
})
@ToString(exclude = {"room", "booking", "assignedTo", "createdBy"})
public class HousekeepingTask {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "task_id")
    private Long taskId;

    // FK -> Room
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

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


    // FK -> Booking
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "booking_id")
    private Booking booking;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn (name = "assigned_to")
    private StaffAccount assignedTo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by")
    private StaffAccount createdBy;

    @Column(name = "task_type")
    private String taskType;


    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "note_housekeeping")
    private String noteHousekeeping;
    @Column(name = "updated_at")
    private String updatedAt;

    public HousekeepingTask(Room room, TaskStatus status, PriorityStatus priority, Booking booking, LocalDateTime createdAt, String taskType, String notes) {
        this.room = room;
        this.status = status;
        this.priority = priority;
        this.booking = booking;
        this.createdAt = createdAt;
        this.taskType = taskType;
        this.notes = notes;
    }

    public LocalDate date(){
        return createdAt.toLocalDate();
    }
    public LocalTime createTime(){
        return createdAt.toLocalTime();
    }

    public String dateStart() {
        return startedAt != null ? startedAt.toLocalDate().toString() : "";
    }

    public String timeStart() {
        return startedAt != null ? startedAt.toLocalTime().toString() : "";
    }

    public LocalTime completedTime(){
        return completedAt.toLocalTime();
    }


}
