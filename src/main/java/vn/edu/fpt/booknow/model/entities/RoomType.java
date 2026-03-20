package vn.edu.fpt.booknow.model.entities;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "RoomType")
public class RoomType {

    // ===== PRIMARY KEY =====
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "room_type_id")
    private Long roomTypeId;

    // ===== NAME =====
    @Column(name = "name", length = 100, nullable = false)
    private String name;

    // ===== DESCRIPTION =====
    @Column(name = "description", length = 500)
    private String description;

    // ===== BASE PRICE =====
    @Column(name = "base_price", precision = 12, scale = 2)
    private BigDecimal basePrice;

    // ===== OVER PRICE =====
    @Column(name = "over_price", precision = 12, scale = 2)
    private BigDecimal overPrice;

    // ===== IMAGE =====
    @Column(name = "image_url", length = 500)
    private String imageUrl;

    // ===== MAX GUESTS =====
    @Column(name = "max_guests", nullable = false)
    private Integer maxGuests;

    // ===== ROOM AREA =====
    @Column(name = "area_m2", precision = 10, scale = 2)
    private BigDecimal areaM2;

    // ===== SOFT DELETE =====
    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    // ===== RELATIONSHIP WITH ROOM =====
    @OneToMany(mappedBy = "roomType", fetch = FetchType.LAZY)
    private List<Room> rooms;

    // ===== CONSTRUCTOR =====
    public RoomType() {
    }

    // ===== GETTERS & SETTERS =====

    public Long getRoomTypeId() {
        return roomTypeId;
    }

    public void setRoomTypeId(Long roomTypeId) {
        this.roomTypeId = roomTypeId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Integer getMaxGuests() {
        return maxGuests;
    }

    public void setMaxGuests(Integer maxGuests) {
        this.maxGuests = maxGuests;
    }

    public BigDecimal getAreaM2() {
        return areaM2;
    }

    public void setAreaM2(BigDecimal areaM2) {
        this.areaM2 = areaM2;
    }

    public Boolean getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(Boolean isDeleted) {
        this.isDeleted = isDeleted;
    }

    public List<Room> getRooms() {
        return rooms;
    }

    public void setRooms(List<Room> rooms) {
        this.rooms = rooms;
    }
}