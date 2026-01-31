package vn.edu.fpt.booknow.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "Image")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Image {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "image_id")
    private Long imageId;

    @ManyToOne
    @JoinColumn(name = "room_id")
    private Room room;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "is_cover")
    private Boolean isCover;
}
