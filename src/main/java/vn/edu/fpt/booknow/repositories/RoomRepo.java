package vn.edu.fpt.booknow.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import vn.edu.fpt.booknow.dto.RoomDTO;
import vn.edu.fpt.booknow.entities.Room;

import java.util.List;

public interface RoomRepo extends JpaRepository<Room,Long> {
    @Query("SELECT DISTINCT new vn.edu.fpt.booknow.dto.RoomDTO(r.roomId,t.basePrice,t.maxGuests,t.name,t.description,i.imageUrl,m.name,m.iconUrl)  FROM Room r \n" +
            "JOIN RoomAmenity a ON r.roomId = a.roomAmenityId \n" +
            "JOIN RoomType t ON t.roomTypeId = r.roomType.roomTypeId\n" +
            "JOIN Amenity m ON m.amenityId= a.roomAmenityId\n" +
            "JOIN Image i ON i.room.roomId = r.roomId \n" +
            "WHERE r.isDeleted = false AND i.isCover = true")
    Page<RoomDTO> findRoom(Pageable pageable);

}
