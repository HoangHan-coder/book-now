package vn.edu.fpt.booknow.model.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Nationalized;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
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

    @OneToMany(mappedBy = "amenity")
    private List<RoomAmenity> roomAmenities = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Size(max = 255)
    @Nationalized
    @Column(name = "icon_public_id")
    private String iconPublicId;

    public Amenity() {
    }

    public Amenity(String name, String iconUrl) {
        this.name = name;
        this.iconUrl = iconUrl;
    }
}
