package vn.edu.fpt.booknow.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Nationalized;

@Getter
@Setter
@Entity
@Table(name = "Amenity")
public class Amenity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "amenity_id", nullable = false)
    private Long id;


    @Column(name = "name", nullable = false, length = 100)
    private String name;


    @Column(name = "icon_url")
    private String iconUrl;


    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted;


}