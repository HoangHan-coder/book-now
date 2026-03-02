package vn.edu.fpt.booknow.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.edu.fpt.booknow.dto.RoomDTO;
import vn.edu.fpt.booknow.entities.Room;

import java.util.List;

public interface RoomRepository extends JpaRepository<Room, Long> {
    @Query("SELECT DISTINCT new vn.edu.fpt.booknow.dto.RoomDTO(r.roomId,t.basePrice,t.maxGuests,r.roomNumber,t.name,t.description,i.imageUrl,m.name,m.iconUrl,t.overPrice,null)  FROM Room r \n" +
            "JOIN RoomAmenity a ON r.roomId = a.roomAmenityId \n" +
            "JOIN RoomType t ON t.roomTypeId = r.roomType.roomTypeId\n" +
            "JOIN Amenity m ON m.amenityId= a.roomAmenityId\n" +
            "JOIN Image i ON i.room.roomId = r.roomId \n" +
            "WHERE r.isDeleted = false AND i.isCover = true")
    Page<RoomDTO> findRoom(Pageable pageable);
    @Query(
            value = """
        SELECT DISTINCT new vn.edu.fpt.booknow.dto.RoomDTO(
            r.roomId, t.basePrice, t.maxGuests, r.roomNumber, t.name, t.description, 
            i.imageUrl, null, null, t.overPrice, null
        )
        FROM Room r
        JOIN r.roomType t 
        JOIN Image i ON i.room.roomId = r.roomId 
        WHERE r.isDeleted = false
          AND i.isCover = true
          AND (:keyword IS NULL OR LOWER(t.name) LIKE LOWER(CONCAT('%', :keyword, '%')))
          AND (:maxGuest IS NULL OR t.maxGuests = :maxGuest)
          AND (
                :price IS NULL OR :price = ''
                OR (:price = 'low'  AND t.basePrice < 300000)
                OR (:price = 'mid'  AND t.basePrice BETWEEN 300000 AND 800000)
                OR (:price = 'high' AND t.basePrice > 800000)
              )
          AND (:amenityIds IS NULL OR EXISTS (
                SELECT 1 FROM RoomAmenity ra 
                JOIN ra.amenity am 
                WHERE ra.room.roomId = r.roomId AND am.name IN :amenityIds
              ))
    """,
            countQuery = """
        SELECT COUNT(DISTINCT r.roomId)
        FROM Room r
        JOIN r.roomType t
        JOIN Image i ON i.room.roomId = r.roomId
        WHERE r.isDeleted = false
          AND i.isCover = true
          AND (:keyword IS NULL OR LOWER(t.name) LIKE LOWER(CONCAT('%', :keyword, '%')))
          AND (:maxGuest IS NULL OR t.maxGuests = :maxGuest)
          AND (:price IS NULL OR :price = ''
                OR (:price = 'low'  AND t.basePrice < 300000)
                OR (:price = 'mid'  AND t.basePrice BETWEEN 300000 AND 800000)
                OR (:price = 'high' AND t.basePrice > 800000)
              )
          AND (:amenityIds IS NULL OR EXISTS (
                SELECT 1 FROM RoomAmenity ra 
                JOIN ra.amenity am 
                WHERE ra.room.roomId = r.roomId AND am.name IN :amenityIds
              ))
    """
    )
    Page<RoomDTO> searchRooms(
            @Param("keyword") String keyword,
            @Param("area") String area,
            @Param("maxGuest") Integer maxGuest,
            @Param("price") String price,
            @Param("amenityIds") List<String> amenityIds,
            Pageable pageable
    );

    @Query("SELECT DISTINCT new vn.edu.fpt.booknow.dto.RoomDTO(r.roomId,t.basePrice,t.maxGuests,r.roomNumber,t.name,t.description,i.imageUrl,m.name,m.iconUrl,t.overPrice,null)  FROM Room r \n" +
            "JOIN RoomAmenity a ON r.roomId = a.room.roomId \n" +
            "JOIN RoomType t ON t.roomTypeId = r.roomType.roomTypeId\n" +
            "JOIN Amenity m ON m.amenityId= a.roomAmenityId\n" +
            "JOIN Image i ON i.room.roomId = r.roomId \n" +
            "WHERE r.isDeleted = false AND i.isCover = true AND r.roomId = :id")
    List<RoomDTO> findRoomDetail(@Param("id") Long id);

    @Query("""
                SELECT new vn.edu.fpt.booknow.dto.RoomDTO(
                    r.roomId,
                    t.basePrice,
                    t.maxGuests,
                    r.roomNumber,
                    t.name,
                    t.description,
                    i.imageUrl,
                    null,
                    null,
                    t.overPrice,
                    null
                )
                FROM Room r
                JOIN r.roomType t
                JOIN r.images i
                WHERE r.isDeleted = false
                  AND i.isCover = true
                GROUP BY r.roomId, t.basePrice, t.maxGuests,r.roomNumber, t.name, t.description, i.imageUrl, t.overPrice
            """)
// Spring sẽ tự động nối ORDER BY dựa vào tham số Sort truyền vào
    List<RoomDTO> findAllRoomsSorted(Sort sort);

    @Query("SELECT DISTINCT new vn.edu.fpt.booknow.dto.RoomDTO(r.roomId,t.basePrice,t.maxGuests,r.roomNumber, t.name,t.description,i.imageUrl,m.name,m.iconUrl,t.overPrice,null)  FROM Room r \n" +
            "JOIN RoomAmenity a ON r.roomId = a.roomAmenityId \n" +
            "JOIN RoomType t ON t.roomTypeId = r.roomType.roomTypeId\n" +
            "JOIN Amenity m ON m.amenityId= a.roomAmenityId\n" +
            "JOIN Image i ON i.room.roomId = r.roomId \n" +
            "WHERE r.isDeleted = false AND i.isCover = true")
    List<RoomDTO> findAllRoom();
}
