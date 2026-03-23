package vn.edu.fpt.booknow.model.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "RoomAmenity", schema = "dbo", uniqueConstraints = {
        @UniqueConstraint(name = "UQ_RoomAmenity_Room_Amenity", columnNames = {"room_id", "amenity_id"})
})
public class RoomAmenity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "room_amenity_id", nullable = false)
    private Long roomAmenityId;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "amenity_id", nullable = false)
    private Amenity amenity;

}
