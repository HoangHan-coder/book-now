package vn.edu.fpt.booknow.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WorkShift {

    private LocalDateTime workDate;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private String shiftType;





}
