package vn.edu.fpt.booknow.model.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Nationalized;

@Getter
@Setter
@Entity
@Table(name = "Amenity", schema = "dbo", uniqueConstraints = {
        @UniqueConstraint(name = "UQ_Amenity_Name", columnNames = {"name"})
})
public class Amenity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "amenity_id", nullable = false)
    private Long amenityId;

    @Size(max = 100)
    @NotNull
    @Nationalized
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Size(max = 255)
    @Nationalized
    @Column(name = "icon_url")
    private String iconUrl;

    @NotNull
    @ColumnDefault("0")
    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

}