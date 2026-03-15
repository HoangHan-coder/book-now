package vn.edu.fpt.booknow.model.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Nationalized;

import java.time.LocalTime;
import java.util.ArrayList;

import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Timetable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "timetable_id", nullable = false)
    private Long timetableId;

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