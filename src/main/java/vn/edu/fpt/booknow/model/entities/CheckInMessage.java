package vn.edu.fpt.booknow.model.entities;

public class CheckInMessage {
    private Long checkInId;
    private Long bookingId;
    private String videoUrl;
    private String status;

    public CheckInMessage() {
    }

    public CheckInMessage(Long checkInId, Long bookingId, String videoUrl, String status) {
        this.checkInId = checkInId;
        this.bookingId = bookingId;
        this.videoUrl = videoUrl;
        this.status = status;
    }

    public Long getCheckInId() {
        return checkInId;
    }

    public Long getBookingId() {
        return bookingId;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public String getStatus() {
        return status;
    }
}
