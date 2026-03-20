package vn.edu.fpt.booknow.model.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Nationalized;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "TaskChecklist", schema = "dbo", indexes = {
        @Index(name = "IX_TaskChecklist_TaskId", columnList = "task_id")
})
public class TaskChecklist {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "checklist_id", nullable = false)
    private Long checklistId;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "task_id", nullable = false)
    private HousekeepingTask task;

    @Size(max = 200)
    @NotNull
    @Nationalized
    @Column(name = "item_name", nullable = false, length = 200)
    private String itemName;

    @NotNull
    @ColumnDefault("0")
    @Column(name = "is_completed", nullable = false)
    private Boolean isCompleted = false;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

}