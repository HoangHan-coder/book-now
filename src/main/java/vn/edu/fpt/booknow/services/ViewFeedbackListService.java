package vn.edu.fpt.booknow.services;

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

    public Page<FeedbackListDTO> getFeedbackList(Integer rating,
            Boolean hidden,
            String keyword,
            int page,
            int size) {

        // Normalize keyword
        final String keywordNormalized = (keyword == null || keyword.isBlank()) ? null : normalize(keyword.trim());

        Pageable pageable = PageRequest.of(page, size);

        // BỎ keyword khỏi quer
        Page<Feedback> feedbackPage = feedbackRepository.filterFeedback(rating, hidden, pageable);

        // Filter lại bằng Java
        List<FeedbackListDTO> filteredList = feedbackPage.getContent().stream()
                .filter(f -> {

                    if (keywordNormalized == null)
                        return true;

                    String customerName = f.getBooking().getCustomer().getFullName();
                    String bookingCode = f.getBooking().getBookingCode();

                    // normalize
                    String nameNormalized = vn.edu.fpt.booknow.utils.TextUtils
                            .removeAccent(customerName.toLowerCase());

                    String bookingNormalized = normalize(bookingCode);

                    // match name OR bookingCode
                    return matchName(customerName, keywordNormalized)
                            || matchBookingCode(bookingCode, keywordNormalized);
                })
                .map(f -> new FeedbackListDTO(
                        f.getFeedbackId(),
                        f.getRating(),
                        f.getBooking().getCustomer().getFullName(),
                        f.getBooking().getRoom().getRoomNumber(),
                        f.getIsHidden(),
                        f.getCreatedAt()))
                .toList();

        // Convert lại Page
        return new org.springframework.data.domain.PageImpl<>(
                filteredList,
                pageable,
                filteredList.size());
    }

    private String normalize(String input) {
        if (input == null)
            return null;

        String noAccent = vn.edu.fpt.booknow.utils.TextUtils.removeAccent(input);

        return noAccent.replaceAll("[^0-9a-zA-Z]", "").toLowerCase();
    }

    private boolean matchName(String name, String keyword) {
        return normalize(name).contains(normalize(keyword));
    }

    private boolean matchBookingCode(String booking, String keyword) {

        String bookingNorm = normalize(booking);
        String keywordNorm = normalize(keyword);

        // nếu là số, so đuôi (tránh match sai)
        if (keywordNorm.matches("\\d+")) {
            return bookingNorm.endsWith(keywordNorm);
        }

        return bookingNorm.contains(keywordNorm);
    }

}