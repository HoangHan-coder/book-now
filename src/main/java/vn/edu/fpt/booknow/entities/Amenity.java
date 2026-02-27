package vn.edu.fpt.booknow.entities;

import jakarta.persistence.*;

import java.util.List;

@Entity
public class Amenity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "amenity_id")
    private Long amenityId;

    private String name;

    @OneToMany(mappedBy = "amenity")
    private List<RoomAmenity> roomAmenities;

    public Amenity() {
    }

    public Amenity(Long amenityId, String name, List<RoomAmenity> roomAmenities) {
        this.amenityId = amenityId;
        this.name = name;
        this.roomAmenities = roomAmenities;
    }

    public Long getAmenityId() {
        return amenityId;
    }

    public void setAmenityId(Long amenityId) {
        this.amenityId = amenityId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<RoomAmenity> getRoomAmenities() {
        return roomAmenities;
    }

    public void setRoomAmenities(List<RoomAmenity> roomAmenities) {
        this.roomAmenities = roomAmenities;
    }
}
