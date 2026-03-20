package vn.edu.fpt.booknow.model.entities;

import jakarta.persistence.*;
import org.hibernate.annotations.ColumnDefault;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "room")
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "room_id")
    private Long roomId;

    // ===== ROOM TYPE =====
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_type_id", nullable = false)
    private RoomType roomType;

    // ===== ROOM NUMBER =====
    @Column(name = "room_number", length = 50, nullable = false)
    private String roomNumber;

    // ===== STATUS =====
    @Enumerated(EnumType.STRING)
    @ColumnDefault("'AVAILABLE'")
    @Column(name = "status", nullable = false, length = 50)
    private RoomStatus status;

    public RoomStatus getStatus() {
        return status;
    }

    public void setStatus(RoomStatus status) {
        this.status = status;
    }

    // ===== SOFT DELETE =====
    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    // ===== ROOM IMAGES =====
    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL)
    private Set<Image> images = new HashSet<>();

    // ===== ROOM AMENITIES =====
    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<RoomAmenity> roomAmenities = new HashSet<>();

    public Room() {}

    // ===== GETTERS SETTERS =====

    public Long getRoomId() {
        return roomId;
    }

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }

    public RoomType getRoomType() {
        return roomType;
    }

    public void setRoomType(RoomType roomType) {
        this.roomType = roomType;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }


    public Boolean getDeleted() {
        return isDeleted;
    }

    public void setDeleted(Boolean deleted) {
        isDeleted = deleted;
    }

    public Set<Image> getImages() {
        return images;
    }

    public void setImages(Set<Image> images) {
        this.images = images;
    }

    public Set<RoomAmenity> getRoomAmenities() {
        return roomAmenities;
    }

    public void setRoomAmenities(Set<RoomAmenity> roomAmenities) {
        this.roomAmenities = roomAmenities;
    }

}