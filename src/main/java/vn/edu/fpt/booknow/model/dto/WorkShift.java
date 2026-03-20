package vn.edu.fpt.booknow.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WorkShift {

    private LocalDateTime workDate;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private String shiftType;





}
