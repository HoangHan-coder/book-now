package vn.edu.fpt.booknow.controllers.model.entities;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.List;

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
    @Column(name = "status", length = 50, nullable = false)
    private String status;

    // ===== SOFT DELETE =====
    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    // ===== AREA =====
    @Column(name = "area_m2", precision = 10, scale = 2)
    private BigDecimal areaM2;

    // ===== BASE PRICE =====
    @Column(name = "base_price", precision = 12, scale = 2)
    private BigDecimal basePrice;

    // ===== EXTRA PRICE =====
    @Column(name = "over_price", precision = 12, scale = 2)
    private BigDecimal overPrice;

    // ===== DESCRIPTION =====
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    // ===== ROOM IMAGES =====
    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL)
    private List<Image> images;

    // ===== ROOM AMENITIES =====
    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RoomAmenity> roomAmenities;

    // ===== Constructors =====
    public Room() {}

    // ===== Getters & Setters =====

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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Boolean getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(Boolean isDeleted) {
        this.isDeleted = isDeleted;
    }

    public BigDecimal getAreaM2() {
        return areaM2;
    }

    public void setAreaM2(BigDecimal areaM2) {
        this.areaM2 = areaM2;
    }

    public BigDecimal getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(BigDecimal basePrice) {
        this.basePrice = basePrice;
    }

    public BigDecimal getOverPrice() {
        return overPrice;
    }

    public void setOverPrice(BigDecimal overPrice) {
        this.overPrice = overPrice;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<Image> getImages() {
        return images;
    }

    public void setImages(List<Image> images) {
        this.images = images;
    }

    public List<RoomAmenity> getRoomAmenities() {
        return roomAmenities;
    }

    public void setRoomAmenities(List<RoomAmenity> roomAmenities) {
        this.roomAmenities = roomAmenities;
    }
}