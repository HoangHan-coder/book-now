package vn.edu.fpt.booknow.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import vn.edu.fpt.booknow.entities.Room;

import java.util.List;

public interface RoomRepo extends JpaRepository<Room,Long> {
    @Query("SELECT TOP 3 r.room_id,t.base_price,t.max_guests,t.name,t.description,i.image_url,m.name,m.icon_url FROM Room r \n" +
            "JOIN Room_Amenity a ON r.room_id = a.amenity_id \n" +
            "JOIN RoomType t ON t.room_type_id = r.room_type_id\n" +
            "JOIN Amenity m ON m.amenity_id = a.amenity_id\n" +
            "JOIN Image i ON i.room_id = r.room_id \n" +
            "WHERE r.is_deleted = 'false'")
    List<Room> findRoom();

}
