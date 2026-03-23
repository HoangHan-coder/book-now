package vn.edu.fpt.booknow.services;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.edu.fpt.booknow.model.entities.Feedback;
import vn.edu.fpt.booknow.repositories.FeedbackRepository;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ReplyFeedbackService {

    private final FeedbackRepository feedbackRepository;

    /**
     * UC-14.4: Reply Feedback
     * Saves staff reply for a feedback entry.
     */
    @Transactional
    public void saveReply(Long feedbackId, String replyContent, Long adminId) {

        // Validate feedback existence
        Feedback feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new RuntimeException("Feedback not found"));

        // Business Rule BR3: Do not modify original feedback content
        feedbackRepository.updateReply(feedbackId, replyContent, adminId, LocalDateTime.now());
    }
}