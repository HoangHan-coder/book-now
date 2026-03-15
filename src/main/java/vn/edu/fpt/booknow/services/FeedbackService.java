package vn.edu.fpt.booknow.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.fpt.booknow.model.entities.Booking;
import vn.edu.fpt.booknow.model.entities.Feedback;
import vn.edu.fpt.booknow.repositories.BookingRepository;
import vn.edu.fpt.booknow.repositories.FeedbackRepository;

@Service
public class FeedbackService {

    @Autowired
    private FeedbackRepository feedbackRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Transactional
    public Feedback createFeedback(Long bookingId, Integer rating, String content) {
        Booking booking = bookingRepository.getReferenceById(bookingId);
        return feedbackRepository.save(new Feedback(booking, content, rating));
    }

    public boolean hasFeedback(Long bookingId) {
        return feedbackRepository.findFeedbacksByBooking_BookingId(bookingId).isPresent();
    }

}
