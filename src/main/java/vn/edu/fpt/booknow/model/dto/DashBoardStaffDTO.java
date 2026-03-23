package vn.edu.fpt.booknow.model.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.edu.fpt.booknow.model.entities.Booking;
import vn.edu.fpt.booknow.model.entities.Room;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashBoardStaffDTO {
    private long newBookings;
    private int checkinPending;
    private long newFeedback;
    private int availableRooms;
    private int totalRooms;

    private List<Booking> bookings;
    private List<Room> rooms;
}
