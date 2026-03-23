package vn.edu.fpt.booknow.model.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.edu.fpt.booknow.model.entities.Booking;
import vn.edu.fpt.booknow.model.entities.BookingStatus;
import vn.edu.fpt.booknow.model.entities.Customer;
import vn.edu.fpt.booknow.model.entities.Room;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class BookingDTO {
    private Long bookingId;

    private Customer customer;

    private Room room;

    private LocalDateTime checkInTime;

    private LocalDateTime checkOutTime;

    private LocalDateTime actualCheckInTime;

    private LocalDateTime actualCheckOutTime;

    private String idCardFrontUrl;

    private String idCardBackUrl;

    private BookingStatus bookingStatus;

    private BigDecimal totalAmount;

    private String bookingCode;

    private LocalDateTime createdAt;

    private String note;

    private LocalDateTime updateAt;

    public BookingDTO(Booking booking) {
        this.bookingId = booking.getBookingId();
        this.customer = booking.getCustomer();
        this.room = booking.getRoom();
        this.checkInTime = booking.getCheckInTime();
        this.checkOutTime = booking.getCheckOutTime();
        this.actualCheckInTime = booking.getActualCheckInTime();
        this.actualCheckOutTime = booking.getActualCheckOutTime();
        this.idCardFrontUrl = booking.getIdCardFrontUrl();
        this.idCardBackUrl = booking.getIdCardBackUrl();
        this.bookingStatus = booking.getBookingStatus();
        this.totalAmount = booking.getTotalAmount();
        this.bookingCode = booking.getBookingCode();
        this.createdAt = booking.getCreatedAt();
        this.note = booking.getNote();
        this.updateAt = booking.getUpdatedAt();
    }

    public String getCheckInDate() {
        return checkInTime.toLocalDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
    }

    public String getCheckOutDate() {
        return checkOutTime.toLocalDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
    }

    public String getCreateAt() {
        return createdAt.format(DateTimeFormatter.ofPattern("HH:mm dd-MM-yyyy"));
    }

    public String getCheckInTime() {
        return checkInTime.format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    public String getCheckOutTime() {
        return checkOutTime.format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    public String getTotalAmountVND() {
        return NumberFormat.getCurrencyInstance(Locale.forLanguageTag("vi-VN")).format(totalAmount);
    }

    public String getActualCheckInDate() {
        return actualCheckInTime != null ? actualCheckInTime.toLocalDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")) : null;
    }

    public String getActualCheckOutDate() {
        return actualCheckOutTime != null ? actualCheckOutTime.toLocalDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")) : null;
    }

    public String getActualCheckInTime() {
        return actualCheckInTime != null ? actualCheckInTime.format(DateTimeFormatter.ofPattern("HH:mm")) : null;
    }

    public String getActualCheckOutTime() {
        return actualCheckOutTime != null ? actualCheckOutTime.format(DateTimeFormatter.ofPattern("HH:mm")) : null;
    }

    public boolean isOverStay() {
        LocalDateTime now = LocalDateTime.now();
        return actualCheckOutTime == null && checkOutTime.isBefore(now);
    }
}
