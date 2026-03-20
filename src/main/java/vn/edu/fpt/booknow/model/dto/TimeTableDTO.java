package vn.edu.fpt.booknow.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.edu.fpt.booknow.model.entities.BookingStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TimeTableDTO {
    private Long bookingId;
    private Long roomId;
    private BookingStatus bookingStatus;
    private BigDecimal totalAmount;
    private Long timetableId;
    private LocalDateTime date;
}
