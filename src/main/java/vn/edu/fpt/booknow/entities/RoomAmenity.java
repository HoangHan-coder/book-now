package vn.edu.fpt.booknow.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "RoomAmenity")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RoomAmenity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "room_amenity_id")
    private Long roomAmenityId;

    @ManyToOne
    @JoinColumn(name = "room_id")
    private Room room;

    @ManyToOne
    @JoinColumn(name = "amenity_id")
    private Amenity amenity;
}
