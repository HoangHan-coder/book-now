package vn.edu.fpt.booknow.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.edu.fpt.booknow.model.entities.Feedback;
import vn.edu.fpt.booknow.repositories.FeedbackRepository;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FeedbackDetailService {

    private final FeedbackRepository feedbackRepository;

    // UC-14.2: View Feedback Detail
    public Feedback getFeedbackDetail(Long feedbackId) {
        if (feedbackId == null) {
            throw new IllegalArgumentException("Feedback ID must not be null");
        }
        // Dùng hàm có JOIN FETCH để tránh lỗi Lazy Loading
        return feedbackRepository.findDetailById(feedbackId)
                .orElseThrow(() -> new RuntimeException("Feedback not found"));
    }
}