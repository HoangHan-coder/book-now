package vn.edu.fpt.booknow.model.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Nationalized;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "Timetable", schema = "dbo")
public class Timetable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "timetable_id", nullable = false)
    private Long timetableId;

    @Size(max = 50)
    @NotNull
    @Nationalized
    @Column(name = "slot_name", nullable = false, length = 50)
    private String slotName;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @OneToMany(mappedBy = "timetable")
    private List<Scheduler> schedulers = new ArrayList<>();

}