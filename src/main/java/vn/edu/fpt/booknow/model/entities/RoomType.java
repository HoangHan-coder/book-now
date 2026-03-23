package vn.edu.fpt.booknow.model.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Nationalized;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "RoomType", schema = "dbo", uniqueConstraints = {
                @UniqueConstraint(name = "UQ_RoomType_Name", columnNames = { "name" })
})
public class RoomType {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Column(name = "room_type_id", nullable = false)
        private Long roomTypeId;

        @Size(max = 100)
        @NotNull
        @Nationalized
        @Column(name = "name", nullable = false, length = 100)
        private String name;

        @Size(max = 500)
        @Nationalized
        @Column(name = "description", length = 500)
        private String description;

        @Column(name = "base_price", precision = 12, scale = 2)
        private BigDecimal basePrice;

        @Column(name = "over_price", precision = 12, scale = 2)
        private BigDecimal overPrice;

        @Size(max = 500)
        @Nationalized
        @Column(name = "image_url", length = 500)
        private String imageUrl;

        @NotNull
        @Column(name = "max_guests", nullable = false)
        private Integer maxGuests;

        @Column(name = "area_m2", precision = 10, scale = 2)
        private BigDecimal areaM2;

        @NotNull
        @ColumnDefault("0")
        @Column(name = "is_deleted", nullable = false)
        private Boolean isDeleted;

        @OneToMany(mappedBy = "roomType")
        private List<Room> rooms = new ArrayList<>();
}
