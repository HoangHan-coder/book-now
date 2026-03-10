package vn.edu.fpt.booknow.dto;

import vn.edu.fpt.booknow.entities.BookingStatus;

import java.time.LocalDateTime;

public class BookingUpdateMessage {
    private String bookingCode;
    private BookingStatus bookingStatus;
    private String customerName;
    private String roomNumber;
    private LocalDateTime checkInTime;
    private LocalDateTime checkOutTime;
    private LocalDateTime updateAt;
    private String action; // "CREATED", "UPDATED", "STATUS_CHANGED"

    public BookingUpdateMessage() {
    }

    public BookingUpdateMessage(String bookingCode, BookingStatus bookingStatus, 
                                String customerName, String roomNumber,
                                LocalDateTime checkInTime, LocalDateTime checkOutTime,
                                LocalDateTime updateAt, String action) {
        this.bookingCode = bookingCode;
        this.bookingStatus = bookingStatus;
        this.customerName = customerName;
        this.roomNumber = roomNumber;
        this.checkInTime = checkInTime;
        this.checkOutTime = checkOutTime;
        this.updateAt = updateAt;
        this.action = action;
    }

    // Getters and Setters
    public String getBookingCode() {
        return bookingCode;
    }

    public void setBookingCode(String bookingCode) {
        this.bookingCode = bookingCode;
    }

    public BookingStatus getBookingStatus() {
        return bookingStatus;
    }

    public void setBookingStatus(BookingStatus bookingStatus) {
        this.bookingStatus = bookingStatus;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
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

    public LocalDateTime getUpdateAt() {
        return updateAt;
    }

    public void setUpdateAt(LocalDateTime updateAt) {
        this.updateAt = updateAt;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }
}
