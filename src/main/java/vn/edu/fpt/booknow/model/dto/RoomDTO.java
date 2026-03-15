package vn.edu.fpt.booknow.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import vn.edu.fpt.booknow.model.entities.Room;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class RoomDTO {

    private Long roomId;

    private RoomTypeDTO roomType;

    private String roomNumber;

    private String status;

    private Boolean isDeleted = false;

    public RoomDTO(Room room) {
        this.roomId = room.getRoomId();
        this.roomType = new RoomTypeDTO(room.getRoomType());
        this.roomNumber = room.getRoomNumber();
        this.status = room.getStatus();
        this.isDeleted = room.getIsDeleted();
    }

}
