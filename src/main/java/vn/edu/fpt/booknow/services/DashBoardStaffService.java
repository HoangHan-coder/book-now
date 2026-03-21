package vn.edu.fpt.booknow.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.fpt.booknow.model.dto.DashBoardStaffDTO;
import vn.edu.fpt.booknow.model.entities.Booking;
import vn.edu.fpt.booknow.model.entities.BookingStatus;
import vn.edu.fpt.booknow.model.entities.Room;
import vn.edu.fpt.booknow.repositories.BookingRepository;
import vn.edu.fpt.booknow.repositories.FeedbackRepository;
import vn.edu.fpt.booknow.repositories.RoomRepository;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;


@Service
public class DashBoardStaffService {


    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private FeedbackRepository feedbackRepository;

    @Transactional(readOnly = true)
    public DashBoardStaffDTO getDashboard(LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.atTime(23, 59, 59);

        // 1. Số đơn mới hôm nay
        long newBookings = bookingRepository.countByCreatedAtBetween(start, end);

        // 2. Danh sách booking check-in
        List<Booking> bookings = bookingRepository.findByCheckInTimeBetweenAndStatusIn(
                start, end, List.of(BookingStatus.PAID, BookingStatus.CHECKED_IN)
        );

        // 3. Số booking chờ check-in (trạng thái PAID)
        int checkinPending = (int) bookings.stream()
                .filter(b -> b.getBookingStatus() == BookingStatus.PAID)
                .count();

        // 4. Danh sách phòng đã đặt trong ngày
        List<Room> roomsBooked = bookings.stream()
                .map(Booking::getRoom)
                .distinct()
                .toList();

        // 5. Tổng số phòng
        int totalRooms = (int) roomRepository.count();

        // 6. Số phòng còn trống
        int availableRooms = totalRooms - roomsBooked.size();

        // 7. Số phản hồi mới hôm nay
        long newFeedback = feedbackRepository.countByCreatedAtBetween(start, end);

        // Trả về DTO cùng list bookings + rooms
        return new DashBoardStaffDTO(newBookings, checkinPending, newFeedback, availableRooms, totalRooms, bookings, roomsBooked);
    }

}
