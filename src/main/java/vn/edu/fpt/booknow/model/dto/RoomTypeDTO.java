package vn.edu.fpt.booknow.model.dto;

import vn.edu.fpt.booknow.model.entities.RoomType;


import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class RoomTypeDTO {

    private Long id;

    private String name;

    private String description;

    private BigDecimal basePrice;

    private BigDecimal overPrice;

    private String imageUrl;

    private Integer maxGuests;

    private BigDecimal areaM2;

    private Boolean isDeleted = false;

    public RoomTypeDTO(RoomType roomType) {
        this.id = roomType.getRoomTypeId();
        this.name = roomType.getName();
        this.description = roomType.getDescription();
        this.basePrice = roomType.getBasePrice();
        this.overPrice = roomType.getOverPrice();
        this.imageUrl = roomType.getImageUrl();
        this.maxGuests = roomType.getMaxGuests();
        this.areaM2 = roomType.getAreaM2();
        this.isDeleted = roomType.getIsDeleted();
    }

    public String getBasePriceFormated() {
       return basePrice != null ? NumberFormat.getNumberInstance(Locale.forLanguageTag("vi-VN"))
               .format(basePrice) + " VND" : null;
    }

    public String getOverPriceFormated() {
        NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.forLanguageTag("vi-VN"));
        return numberFormat.format(overPrice) + " VND";
    }

}
