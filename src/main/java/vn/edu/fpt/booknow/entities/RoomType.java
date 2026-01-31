package vn.edu.fpt.booknow.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "RoomType")
@Data
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
