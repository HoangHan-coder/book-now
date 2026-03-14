package vn.edu.fpt.booknow.model.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Nationalized;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Feedback {
    @Id
    @Column(name = "feedback_id") // , nullable = false
    private Long feedbackId;

    @ManyToOne(fetch = FetchType.LAZY)  // , optional = false
    @JoinColumn(name = "booking_id")  // , nullable = false
    private Booking booking;

//    @ManyToOne(fetch = FetchType.LAZY, optional = false)
//    @JoinColumn(name = "admin_id", nullable = false)
//    private StaffAccount admin;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id")
    private StaffAccount admin;

    @Column(name = "rating", nullable = false)
    private Integer rating;

    @Nationalized
    @Column(name = "content", nullable = false, length = 1000)
    private String content;

    @Nationalized
    @Column(name = "content_reply", length = 1000)
    private String contentReply;

    @ColumnDefault("0")
    @Column(name = "is_hidden", nullable = false)
    private Boolean isHidden = false;

    @ColumnDefault("sysdatetime()")
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

}