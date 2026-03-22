package vn.edu.fpt.booknow.model.dto;


import java.time.LocalDateTime;

public class NextTimeTableSlotBookingDTO {
    private LocalDateTime checkInTime;
    private LocalDateTime checkOutTime;

    public NextTimeTableSlotBookingDTO() {
    }

    public NextTimeTableSlotBookingDTO(LocalDateTime checkInTime, LocalDateTime checkOutTime) {
        this.checkInTime = checkInTime;
        this.checkOutTime = checkOutTime;
    }

    public LocalDateTime getCheckInTime() {
        return checkInTime;
    }

    public void setCheckInTime(LocalDateTime checkInTime) {
        this.checkInTime = checkInTime;
    }

    public LocalDateTime getCheckOutTime() {
        return checkOutTime;
    }

    public void setCheckOutTime(LocalDateTime checkOutTime) {
        this.checkOutTime = checkOutTime;
    }
}
