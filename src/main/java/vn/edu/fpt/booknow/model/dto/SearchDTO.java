package vn.edu.fpt.booknow.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SearchDTO {
    private String keyword;
    private String area;
    private Integer maxGuest;
    private int page = 0;
    private String price;
    private List<String> amenity;
    private  String sortType;
}
