package vn.edu.fpt.booknow.model.entities;

public class MessageSuccess {

    private String message;
    private BookingStatus bookingStatus;

    public MessageSuccess(String message, BookingStatus bookingStatus) {
        this.message = message;
        this.bookingStatus = bookingStatus;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public BookingStatus getBookingStatus() {
        return bookingStatus;
    }

    public void setBookingStatus(BookingStatus bookingStatus) {
        this.bookingStatus = bookingStatus;
    }
}
