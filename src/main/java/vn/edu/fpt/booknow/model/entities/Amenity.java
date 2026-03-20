package vn.edu.fpt.booknow.model.entities;

import jakarta.persistence.*;

import java.util.List;

@Entity
public class Amenity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "amenity_id")
    private Long amenityId;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "icon_url")
    private String iconUrl;

    // ===== SOFT DELETE =====
    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    // Quan hệ trung gian RoomAmenity
    @OneToMany(mappedBy = "amenity", fetch = FetchType.LAZY)
    private List<RoomAmenity> roomAmenities;

    public Amenity() {
    }

    public Amenity(Long amenityId, String name, String iconUrl) {
        this.amenityId = amenityId;
        this.name = name;
        this.iconUrl = iconUrl;
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

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public List<RoomAmenity> getRoomAmenities() {
        return roomAmenities;
    }

    public void setRoomAmenities(List<RoomAmenity> roomAmenities) {
        this.roomAmenities = roomAmenities;
    }

    public Boolean getDeleted() {
        return isDeleted;
    }

    public void setDeleted(Boolean deleted) {
        isDeleted = deleted;
    }
}
