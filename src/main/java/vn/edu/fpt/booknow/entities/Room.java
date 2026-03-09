package vn.edu.fpt.booknow.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Nationalized;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "Room")
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "room_id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_type_id", nullable = false)
    private RoomType roomType;

    @Column(name = "room_number", nullable = false, length = 50)
    private String roomNumber;


    @Column(name = "status", nullable = false, length = 50)
    private String status;


    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted;

    @Column(name = "area_m2", precision = 10, scale = 2)
    private BigDecimal areaM2;

    @OneToMany(mappedBy = "room")
    private List<Booking> bookings;

}