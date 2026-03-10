package vn.edu.fpt.booknow.model.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Nationalized;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "room_id", nullable = false)
    private Long roomId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_type_id", nullable = false)
    private RoomType roomType;

    @Nationalized
    @Column(name = "room_number", nullable = false, length = 50)
    private String roomNumber;

    @ColumnDefault("'AVAILABLE'")
    @Column(name = "status", nullable = false, length = 50)
    private String status;

    @ColumnDefault("0")
    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @Column(name = "area_m2", precision = 10, scale = 2)
    private BigDecimal areaM2;

    @OneToMany(mappedBy = "room")
    private List<Booking> bookings = new ArrayList<>();

    @OneToMany(mappedBy = "room")
    private List<Image> images = new ArrayList<>();

    @OneToMany(mappedBy = "room")
    private List<RoomAmenity> roomAmenities = new ArrayList<>();
    @Nationalized
    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "base_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal basePrice;
    @Column(name = "over_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal overPrice;
}