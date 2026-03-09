package vn.edu.fpt.booknow.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Nationalized;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "RoomType")
public class RoomType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "room_type_id", nullable = false)
    private Long id;


    @Column(name = "name", nullable = false, length = 100)
    private String name;


    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "base_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal basePrice;


    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "max_guests", nullable = false)
    private Integer maxGuests;


    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted;

    @Column(name = "over_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal overPrice;


}