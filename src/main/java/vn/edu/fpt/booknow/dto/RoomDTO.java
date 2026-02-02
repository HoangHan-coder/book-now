package vn.edu.fpt.booknow.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RoomDTO {
    private Long room_id;
    private Double base_price;
    private Integer max_guest;
    private String name;
    private String description;
    private String image_url;
    private String utilities;
    private String icon_url;

}
