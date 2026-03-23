package vn.edu.fpt.booknow.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.edu.fpt.booknow.model.dto.DetailRoomDTO;
import vn.edu.fpt.booknow.model.entities.Room;
import vn.edu.fpt.booknow.model.entities.RoomStatus;

import java.util.List;
import java.util.Optional;

public interface RoomRepository extends JpaRepository<Room, Long> {
    @Query("SELECT DISTINCT new vn.edu.fpt.booknow.model.dto.DetailRoomDTO(r.roomId,t.basePrice,t.maxGuests,r.roomNumber,t.name,t.description,i.imageUrl,m.name,m.iconUrl,t.overPrice,null)  FROM Room r \n" +
            "JOIN RoomAmenity a ON r.roomId = a.roomAmenityId \n" +
            "JOIN RoomType t ON t.roomTypeId = r.roomType.roomTypeId\n" +
            "JOIN Amenity m ON m.amenityId= a.roomAmenityId\n" +
            "JOIN Image i ON i.room.roomId = r.roomId \n" +
            "WHERE r.isDeleted = false AND i.isCover = true")
    Page<DetailRoomDTO> findRoom(Pageable pageable);
    @Query(
            value = """
    SELECT DISTINCT new vn.edu.fpt.booknow.model.dto.DetailRoomDTO(
        r.roomId, t.basePrice, t.maxGuests, r.roomNumber, t.name, t.description, 
        i.imageUrl, null, null, t.overPrice, null
    )
    FROM Room r
    JOIN r.roomType t 
    LEFT JOIN Image i ON i.room.roomId = r.roomId AND i.isCover = true 
    WHERE r.isDeleted = false
      AND (:keyword IS NULL OR LOWER(r.roomNumber) LIKE LOWER(CONCAT('%', :keyword, '%')))
      AND (:maxGuest IS NULL OR t.maxGuests = :maxGuest)
      AND (
            :price IS NULL OR :price = ''
            OR (:price = 'low'  AND t.basePrice < 300000)
            OR (:price = 'mid'  AND t.basePrice BETWEEN 300000 AND 800000)
            OR (:price = 'high' AND t.basePrice > 800000)
          )
      AND (:amenityIds IS NULL OR (
            SELECT COUNT(DISTINCT ra.amenity.name) 
            FROM RoomAmenity ra 
            WHERE ra.room.roomId = r.roomId 
            AND ra.amenity.name IN :amenityIds
          ) = :amenityCount)
    """,
            countQuery = """
    SELECT COUNT(DISTINCT r.roomId)
    FROM Room r
    JOIN r.roomType t
    LEFT JOIN Image i ON i.room.roomId = r.roomId AND i.isCover = true
    WHERE r.isDeleted = false
      AND (:keyword IS NULL OR LOWER(t.name) LIKE LOWER(CONCAT('%', :keyword, '%')))
      AND (:maxGuest IS NULL OR t.maxGuests = :maxGuest)
      AND (:price IS NULL OR :price = ''
            OR (:price = 'low'  AND t.basePrice < 300000)
            OR (:price = 'mid'  AND t.basePrice BETWEEN 300000 AND 800000)
            OR (:price = 'high' AND t.basePrice > 800000)
          )
      AND (:amenityIds IS NULL OR (
            SELECT COUNT(DISTINCT ra.amenity.name) 
            FROM RoomAmenity ra 
            WHERE ra.room.roomId = r.roomId 
            AND ra.amenity.name IN :amenityIds
          ) = :amenityCount)
    """
    )
    Page<DetailRoomDTO> searchRooms(
            @Param("keyword") String keyword,
            @Param("maxGuest") Integer maxGuest,
            @Param("price") String price,
            @Param("amenityIds") List<String> amenityIds,
            @Param("amenityCount") Long amenityCount,
            Pageable pageable
    );

    @Query("SELECT DISTINCT new vn.edu.fpt.booknow.model.dto.DetailRoomDTO(r.roomId,t.basePrice,t.maxGuests,r.roomNumber,t.name,t.description,i.imageUrl,m.name,m.iconUrl,t.overPrice,null)  FROM Room r \n" +
            "JOIN RoomAmenity a ON r.roomId = a.room.roomId \n" +
            "JOIN RoomType t ON t.roomTypeId = r.roomType.roomTypeId\n" +
            "JOIN Amenity m ON m.amenityId= a.amenity.amenityId\n" +
            "JOIN Image i ON i.room.roomId = r.roomId \n" +
            "WHERE r.isDeleted = false AND i.isCover = true AND r.roomId = :id")
    List<DetailRoomDTO> findRoomDetail(@Param("id") Long id);

    @Query("""
                SELECT new vn.edu.fpt.booknow.model.dto.DetailRoomDTO(
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
    List<DetailRoomDTO> findAllRoomsSorted(Sort sort);

    @Query("SELECT DISTINCT new vn.edu.fpt.booknow.model.dto.DetailRoomDTO(r.roomId,t.basePrice,t.maxGuests,r.roomNumber, t.name,t.description,i.imageUrl,m.name,m.iconUrl,t.overPrice,null)  FROM Room r \n" +
            "JOIN RoomAmenity a ON r.roomId = a.roomAmenityId \n" +
            "JOIN RoomType t ON t.roomTypeId = r.roomType.roomTypeId\n" +
            "JOIN Amenity m ON m.amenityId= a.roomAmenityId\n" +
            "JOIN Image i ON i.room.roomId = r.roomId \n" +
            "WHERE r.isDeleted = false AND i.isCover = true")
    List<DetailRoomDTO> findAllRoom();

    @Query("SELECT r FROM Room r JOIN RoomType t ON r.roomType.roomTypeId = t.roomTypeId WHERE r.roomId = :id")
    Room getPrice(@Param("id") Long id);

    Room getByRoomId(Long roomId);

    boolean existsByRoomNumber(String roomNumber);


    // tổng phòng (không tính DELETED)
    int countByStatusNot(RoomStatus status);


    @EntityGraph(attributePaths = {
            "roomType"
    })
    Page<Room> findAll(Specification<Room> spec, Pageable pageable);

    @EntityGraph(attributePaths = {
            "roomType",
            "images",
            "roomAmenities",
            "roomAmenities.amenity"
    })
    Optional<Room> findById(Long id);

}
