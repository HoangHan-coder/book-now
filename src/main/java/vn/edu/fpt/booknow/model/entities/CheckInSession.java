package vn.edu.fpt.booknow.model.entities;

import jakarta.persistence.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Entity
@Table(name = "CheckInSession")
public class CheckInSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "check_in_session_id")
    private Long checkInSessionId;
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "booking_id")
    private Booking booking;
    @Column(name = "video_url")
    private String videoUrl;
    @Column(name = "video_public_id")
    private String videoPublicId;
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private CheckInSessionStatus status;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by")
    private StaffAccount staffAccount;
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    public CheckInSession() {
    }

    public CheckInSession(Long checkInSessionId, Booking booking, String videoUrl, String videoPublicId, CheckInSessionStatus status, StaffAccount staffAccount, LocalDateTime createdAt, LocalDateTime reviewedAt) {
        this.checkInSessionId = checkInSessionId;
        this.booking = booking;
        this.videoUrl = videoUrl;
        this.videoPublicId = videoPublicId;
        this.status = status;
        this.staffAccount = staffAccount;
        this.createdAt = createdAt;
        this.reviewedAt = reviewedAt;
    }

    public Long getCheckInSessionId() {
        return checkInSessionId;
    }

    public void setCheckInSessionId(Long checkInSessionId) {
        this.checkInSessionId = checkInSessionId;
    }

    public Booking getBooking() {
        return booking;
    }

    public void setBooking(Booking booking) {
        this.booking = booking;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public CheckInSessionStatus getStatus() {
        return status;
    }

    public void setStatus(CheckInSessionStatus status) {
        this.status = status;
    }

    public String getVideoPublicId() {
        return videoPublicId;
    }

    public void setVideoPublicId(String videoPublicId) {
        this.videoPublicId = videoPublicId;
    }

    public StaffAccount getStaffAccount() {
        return staffAccount;
    }

    public void setStaffAccount(StaffAccount staffAccount) {
        this.staffAccount = staffAccount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getReviewedAt() {
        return reviewedAt;
    }

    public void setReviewedAt(LocalDateTime reviewedAt) {
        this.reviewedAt = reviewedAt;
    }
}
