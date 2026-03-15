package vn.edu.fpt.booknow.model.entities;

import jakarta.persistence.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "housekeepingtask", schema = "dbo")
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

    @Column(name = "task_status")
    private String taskStatus;

    @Column(name = "priority")
    private String priority;

    @Column(name = "notes")
    private String notes;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    public LocalDate date(){
        return createdAt.toLocalDate();
    }
    public LocalTime createTime(){
        return createdAt.toLocalTime();
    }

    public LocalDate dateStart(){
        return startedAt.toLocalDate();
    }

    public LocalTime timeStart(){
        return startedAt.toLocalTime();
    }

    public LocalTime completedTime(){
        return completedAt.toLocalTime();
    }


}
