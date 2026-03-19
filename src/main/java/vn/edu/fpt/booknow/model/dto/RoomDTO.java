package vn.edu.fpt.booknow.model.dto;

import lombok.*;
import vn.edu.fpt.booknow.model.entities.Amenity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import vn.edu.fpt.booknow.model.entities.Room;
import vn.edu.fpt.booknow.model.entities.RoomStatus;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class RoomDTO {

    private Long roomId;

    private RoomTypeDTO roomType;

    private String roomNumber;

    private RoomStatus status;

    private Boolean isDeleted = false;

    public RoomDTO(Room room) {
        this.roomId = room.getRoomId();
        this.roomType = new RoomTypeDTO(room.getRoomType());
        this.roomNumber = room.getRoomNumber();
        this.status = room.getStatus();
        this.isDeleted = room.getIsDeleted();
    }

}
