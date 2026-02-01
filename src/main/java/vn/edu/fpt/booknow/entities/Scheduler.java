package vn.edu.fpt.booknow.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "scheduler")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Scheduler {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "scheduler_id")
    private Long schedulerId;

    @ManyToOne
    @JoinColumn(name = "booking_id")
    private Booking booking;

    @ManyToOne
    @JoinColumn(name = "timetable_id")
    private Timetable timetable;
}
