package vn.edu.fpt.booknow.entities;

public class CheckInMessage {

    private final Long bookingId;
    private final String videoUrl;
    private final String status;

    public CheckInMessage(Long bookingId, String videoUrl, String status) {
        this.bookingId = bookingId;
        this.videoUrl = videoUrl;
        this.status = status;
    }

    public Long getBookingId() { return bookingId; }
    public String getVideoUrl() { return videoUrl; }
    public String getStatus() { return status; }
}
