package vn.edu.fpt.booknow.model.dto;

import lombok.*;
import vn.edu.fpt.booknow.model.entities.BookingStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Setter
@Getter
public class TimeTableDTO {
    private Long bookingId;
    private Long roomId;
    private BookingStatus bookingStatus;
    private BigDecimal totalAmount;
    private Long timetableId;
    private LocalDateTime date;

    public TimeTableDTO() {
    }


    public TimeTableDTO(Long bookingId, Long roomId, BookingStatus bookingStatus,
                        BigDecimal totalAmount, Long timetableId, LocalDateTime date) {
        this.bookingId = bookingId;
        this.roomId = roomId;
        this.bookingStatus = bookingStatus; // Safe string conversion
        this.totalAmount = totalAmount;
        this.timetableId = timetableId;
        this.date = date;
    }


}
