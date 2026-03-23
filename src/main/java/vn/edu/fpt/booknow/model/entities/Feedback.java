package vn.edu.fpt.booknow.model.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Nationalized;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "Feedback", schema = "dbo")
public class Feedback {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "feedback_id", nullable = false)
    private Long feedbackId;




    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "booking_id", nullable = false)
    private vn.edu.fpt.booknow.model.entities.Booking booking;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "admin_id", nullable = false)
    private StaffAccount admin;

    @Column(name = "rating", nullable = false)
    private Integer rating;

    @Nationalized
    @Column(name = "content", nullable = false, length = 1000)
    private String content;

    @Size(max = 1000)
    @Nationalized
    @Column(name = "content_reply", length = 1000)
    private String contentReply;

    @NotNull
    @ColumnDefault("0")
    @Column(name = "is_hidden", nullable = false)
    private Boolean isHidden = false;

    @NotNull
    @ColumnDefault("sysdatetime()")
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "reply_at")
    private LocalDateTime replyAt;

    public Feedback() {

    }

    public Feedback(Booking booking, String content, Integer rating) {
        this.booking = booking;
        this.content = content;
        this.rating = rating;
        this.createdAt = LocalDateTime.now();
    }
}