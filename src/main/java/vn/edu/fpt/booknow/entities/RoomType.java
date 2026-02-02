package vn.edu.fpt.booknow.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "roomtype")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RoomType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long roomTypeId;

    private String name;

    private String description;

    private Double basePrice;

    private String imageUrl;

    private Integer maxGuests;

    private Boolean isDeleted;
}
