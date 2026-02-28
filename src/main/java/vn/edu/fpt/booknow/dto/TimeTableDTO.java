package vn.edu.fpt.booknow.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TimeTableDTO {
    private Long bookingId;
    private Long roomId;
    private String bookingStatus;
    private Long totalAmount;
    private Long timetableId;
    private LocalDateTime date;
}
