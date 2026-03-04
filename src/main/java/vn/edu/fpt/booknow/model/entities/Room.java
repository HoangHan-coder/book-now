package vn.edu.fpt.booknow.model.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Nationalized;

import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "room_id", nullable = false)
    private Long id;

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
    private Set<Booking> bookings = new LinkedHashSet<>();

    @OneToMany(mappedBy = "room")
    private Set<Image> images = new LinkedHashSet<>();

    @OneToMany(mappedBy = "room")
    private Set<RoomAmenity> roomAmenities = new LinkedHashSet<>();

}