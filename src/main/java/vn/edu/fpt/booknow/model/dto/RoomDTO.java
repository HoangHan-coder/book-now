package vn.edu.fpt.booknow.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.edu.fpt.booknow.model.entities.Amenity;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RoomDTO {
    private Long roomId;
    private Double basePrice;
    private Integer maxGuest;
    private String roomNumber;
    private String roomType;
    private String description;
    private String imageUrl;
    private String utilities;
    private String iconUrl;
    private Double overPrice;
    List<Amenity> amenityList = new ArrayList<>();

}
