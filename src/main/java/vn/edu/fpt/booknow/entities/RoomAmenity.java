package vn.edu.fpt.booknow.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RoomAmenity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long roomAmenityId;

    @ManyToOne
    @JoinColumn(name = "roomId")
    private Room room;

    @ManyToOne
    @JoinColumn(name = "amenityId")
    private Amenity amenity;
}
