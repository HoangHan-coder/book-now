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
@Table(name = "roomtype")

public class RoomType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "room_type_id", nullable = false)
    private Long roomTypeId;

    @Nationalized
    @Column(name = "name", nullable = false, length = 100)
    private String name;


    @Nationalized
    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "max_guests", nullable = false)
    private Integer maxGuests;

    @ColumnDefault("0")
    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;



    @OneToMany(mappedBy = "roomType")
    private List<Room> rooms = new ArrayList<>();

}