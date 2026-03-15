package vn.edu.fpt.booknow.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vn.edu.fpt.booknow.repositories.FeedbackRepository;

@Service
public class FeedbackService {

    @Autowired
    private FeedbackRepository feedbackRepository;

    public boolean hasFeedback(Long bookingId) {
        return feedbackRepository.findFeedbacksByBooking_BookingId(bookingId).isPresent();
    }
}
