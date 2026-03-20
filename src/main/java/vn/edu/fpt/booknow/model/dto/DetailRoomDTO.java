package vn.edu.fpt.booknow.model.dto;

import lombok.*;
import vn.edu.fpt.booknow.model.entities.Amenity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DetailRoomDTO {
    private Long roomId;
    private BigDecimal basePrice;
    private Integer maxGuest;
    private String roomNumber;
    private String roomType;
    private String description;
    private String imageUrl;
    private String utilities;
    private String iconUrl;
    private BigDecimal overPrice;
    List<Amenity> amenityList = new ArrayList<>();

}
