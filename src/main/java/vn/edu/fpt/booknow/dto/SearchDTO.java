package vn.edu.fpt.booknow.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SearchDTO {
    private String keyword;
    private String area;
    private Integer maxGuest;
    private BigDecimal price;
    private List<String> amenity;
}
