package vn.edu.fpt.booknow.model.entities;

import jakarta.persistence.*;

@Entity
public class RoomAmenity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "room_amenity_id")
    private Long roomAmenityId;

    @ManyToOne
    @JoinColumn(name = "room_id")
    private Room room;

    @ManyToOne
    @JoinColumn (name = "amenity_id")
    private Amenity amenity;

    public RoomAmenity() {
    }

    public RoomAmenity(Long roomAmenityId, Room room, Amenity amenity) {
        this.roomAmenityId = roomAmenityId;
        this.room = room;
        this.amenity = amenity;
    }

    public Long getRoomAmenityId() {
        return roomAmenityId;
    }

    public void setRoomAmenityId(Long roomAmenityId) {
        this.roomAmenityId = roomAmenityId;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    public Amenity getAmenity() {
        return amenity;
    }

    public void setAmenity(Amenity amenity) {
        this.amenity = amenity;
    }
}
