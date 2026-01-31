package vn.edu.fpt.booknow.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "RoomType")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RoomType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "room_type_id")
    private Long roomTypeId;

    private String name;

    private String description;

    @Column(name = "base_price")
    private Long basePrice;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "max_guests")
    private Integer maxGuests;

    @Column(name = "is_deleted")
    private Boolean isDeleted;
}
