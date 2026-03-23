package vn.edu.fpt.booknow.services;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.edu.fpt.booknow.model.entities.Feedback;
import vn.edu.fpt.booknow.repositories.FeedbackRepository;

@Service
@RequiredArgsConstructor
public class FeedbackVisibilityService {

    private final FeedbackRepository feedbackRepository;

    /**
     * UC-14.3: Hide/Show Feedback
     */
    @Transactional
    public void updateVisibility(Long feedbackId, Boolean isHidden) {

        // Retrieve feedback
        Feedback feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() ->
                        new RuntimeException("Feedback not found"));

        // Alternative Flow A2 – Status unchanged
        if (feedback.getIsHidden().equals(isHidden)) {
            return;
        }

        // Update visibility
        feedbackRepository.updateVisibility(feedbackId, isHidden);
    }
}