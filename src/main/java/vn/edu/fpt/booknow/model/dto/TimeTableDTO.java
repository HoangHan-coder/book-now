package vn.edu.fpt.booknow.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.edu.fpt.booknow.model.entities.BookingStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;


public class TimeTableDTO {
    private Long bookingId;
    private Long roomId;
    private BookingStatus bookingStatus;
    private BigDecimal totalAmount;
    private Long timetableId;
    private LocalDateTime date;

    public TimeTableDTO() {
    }

    public TimeTableDTO(Long bookingId, Long roomId, String bookingStatus, BigDecimal totalAmount, Long timetableId, LocalDateTime date) {
        this.bookingId = bookingId;
        this.roomId = roomId;
        this.bookingStatus = bookingStatus;
        this.totalAmount = totalAmount;
        this.timetableId = timetableId;
        this.date = date;
    }

    public TimeTableDTO(Long bookingId, Long roomId, BookingStatus bookingStatus,
                        BigDecimal totalAmount, Long timetableId, LocalDateTime date) {
        this.bookingId = bookingId;
        this.roomId = roomId;
        this.bookingStatus = bookingStatus != null ? bookingStatus.name() : null; // Safe string conversion
        this.totalAmount = totalAmount;
        this.timetableId = timetableId;
        this.date = date;
    }

    public Long getBookingId() {
        return bookingId;
    }

    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }

    public Long getRoomId() {
        return roomId;
    }

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }

    public String getBookingStatus() {
        return bookingStatus;
    }

    public void setBookingStatus(String bookingStatus) {
        this.bookingStatus = bookingStatus;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Long getTimetableId() {
        return timetableId;
    }

    public void setTimetableId(Long timetableId) {
        this.timetableId = timetableId;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }
}
