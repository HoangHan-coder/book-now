package vn.edu.fpt.booknow.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.edu.fpt.booknow.model.dto.TimeTableDTO;
import vn.edu.fpt.booknow.model.entities.Timetable;

import java.util.List;

public interface TimeTableRepository extends JpaRepository<Timetable, Long> {

    @Query("SELECT new vn.edu.fpt.booknow.model.dto.TimeTableDTO(" +
            "    b.bookingId, " +
            "    r.roomId, " +
            "    b.bookingStatus, " +
            "    b.totalAmount, " +
            "    t.timetableId, " +
            "    s.date) " +
            "FROM Booking b " +
            "JOIN Room r ON r.roomId = b.room.roomId  " +
            "JOIN Scheduler s ON s.booking.bookingId = b.bookingId " +
            "JOIN Timetable t ON t.timetableId = s.timetable.timetableId " +
            "WHERE r.roomId = :roomId ")
    List<TimeTableDTO> getBookingDetailsByRoomId(@Param("roomId") Long roomId);

        @Query("SELECT new vn.edu.fpt.booknow.model.dto.TimeTableDTO(" +
            "    b.bookingId, " +
            "    r.roomId, " +
            "    b.bookingStatus, " +
            "    b.totalAmount, " +
            "    t.timetableId, " +
            "    s.date) " +
            "FROM Booking b " +
            "JOIN b.room r " +
            "JOIN b.schedulers s " +
            "JOIN s.timetable t")
    List<TimeTableDTO> getBookingDetails();

}
