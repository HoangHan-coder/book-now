package vn.edu.fpt.booknow.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "Room")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long roomId;

    @ManyToOne
    @JoinColumn(name = "room_type_id")
    private RoomType roomType;

    private String roomNumber;

    private String status;
    private Boolean isDeleted;
    private Double areaM2;
    @OneToMany(mappedBy = "room")
    private List<Image> images;

    @OneToMany(mappedBy = "room")
    private List<RoomAmenity> roomAmenities;
}
