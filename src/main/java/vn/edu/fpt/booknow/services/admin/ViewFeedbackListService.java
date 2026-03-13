package vn.edu.fpt.booknow.services.admin;

import vn.edu.fpt.booknow.model.dto.FeedbackListDTO;
import vn.edu.fpt.booknow.model.entities.Feedback;
import vn.edu.fpt.booknow.repositories.FeedbackRepository;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Service
public class ViewFeedbackListService {

    private final FeedbackRepository feedbackRepository;

    public ViewFeedbackListService(FeedbackRepository feedbackRepository) {
        this.feedbackRepository = feedbackRepository;
    }

    /**
     * UC-14.1 View Feedback List
     * Retrieves feedback records for displaying in the feedback list.
     */
//    public List<FeedbackListDTO> getFeedbackList(Integer rating, Boolean hidden, String keyword) {
//
//        if (keyword != null && keyword.isBlank()) {
//            keyword = null;
//        }
//
//        List<Feedback> feedbacks = feedbackRepository.filterFeedback(rating, hidden, keyword);
//
//        return feedbacks.stream()
//                .map(f -> new FeedbackListDTO(
//                        f.getFeedbackId(),
//                        f.getRating(),
//                        f.getBooking().getCustomer().getFullName(),
//                        f.getBooking().getRoom().getRoomNumber(),
//                        f.getIsHidden(),
//                        f.getCreatedAt()
//                ))
//                .toList();
//    }
    public Page<FeedbackListDTO> getFeedbackList(Integer rating,
                                                 Boolean hidden,
                                                 String keyword,
                                                 int page,
                                                 int size) {

        if (keyword != null && keyword.isBlank()) {
            keyword = null;
        }

        Pageable pageable = PageRequest.of(page, size);

        Page<Feedback> feedbackPage =
                feedbackRepository.filterFeedback(rating, hidden, keyword, pageable);

        return feedbackPage.map(f ->
                new FeedbackListDTO(
                        f.getFeedbackId(),
                        f.getRating(),
                        f.getBooking().getCustomer().getFullName(),
                        f.getBooking().getRoom().getRoomNumber(),
                        f.getIsHidden(),
                        f.getCreatedAt()
                )
        );
    }

}