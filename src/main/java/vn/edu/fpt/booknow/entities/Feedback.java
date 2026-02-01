package vn.edu.fpt.booknow.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "feedback")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Feedback {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "feedback_id")
    private Long feedbackId;

    @ManyToOne
    @JoinColumn(name = "booking_id")
    private Booking booking;

    @ManyToOne
    @JoinColumn(name = "admin_id")
    private StaffAccount admin;

    private Integer rating;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "content_reply", columnDefinition = "TEXT")
    private String contentReply;

    @Column(name = "is_hidden")
    private Boolean isHidden;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
