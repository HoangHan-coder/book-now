package vn.edu.fpt.booknow.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.fpt.booknow.model.dto.FeedbackDetailDTO;
import vn.edu.fpt.booknow.model.dto.FeedbackStatisticsDTO;
import vn.edu.fpt.booknow.model.entities.Booking;
import vn.edu.fpt.booknow.model.entities.Feedback;
import vn.edu.fpt.booknow.repositories.BookingRepository;
import vn.edu.fpt.booknow.repositories.FeedbackRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class FeedbackService {
    private FeedbackRepository feedbackRepository;

    private BookingRepository bookingRepository;
    public FeedbackService(FeedbackRepository feedBackRepository, BookingRepository bookingRepository) {
        this.feedbackRepository = feedBackRepository;
        this.bookingRepository = bookingRepository;
    }
    public Map<String, Object> getRoomFeedbackData(Long roomId) {
        // 1. Lấy danh sách chi tiết
        List<FeedbackDetailDTO> details = feedbackRepository.findFeedbacksByRoomId(roomId);
        System.out.println(details.size() + " detail");
        // 2. Khởi tạo Map kết quả (Đặt tên là feedback để khớp với HTML)
        Map<String, Object> feedback = new HashMap<>();

        if (details.isEmpty()) {
            feedback.put("averageRating", 0.0);
            feedback.put("totalReviews", 0L);
            feedback.put("ratingCounts", createEmptyStarMap());
            feedback.put("feedbackList", new ArrayList<>());
            return feedback;
        }

        // 3. Tính toán con số
        long totalReviews = details.size();
        double averageRating = details.stream()
                .mapToInt(FeedbackDetailDTO::getRating)
                .average()
                .orElse(0.0);
        averageRating = Math.round(averageRating * 10.0) / 10.0;

        // 4. Thống kê số lượng sao
        Map<Integer, Long> ratingCounts = details.stream()
                .collect(Collectors.groupingBy(FeedbackDetailDTO::getRating, Collectors.counting()));

        for (int i = 1; i <= 5; i++) {
            ratingCounts.putIfAbsent(i, 0L);
        }

        // 5. Đóng gói chuẩn theo tên biến trong HTML
        FeedbackStatisticsDTO stats = new FeedbackStatisticsDTO(averageRating, totalReviews, ratingCounts);

        Map<String, Object> response = new HashMap<>();
        System.out.println(stats + " start");
        response.put("stats", stats); // Key là "stats"
        response.put("list", details); // Key là "lis

        return response;
    }

    private Map<Integer, Long> createEmptyStarMap() {
        Map<Integer, Long> map = new HashMap<>();
        for (int i = 1; i <= 5; i++) map.put(i, 0L);
        return map;
    }

    public boolean hasFeedback(Long bookingId) {
        return feedbackRepository.findFeedbacksByBooking_BookingId(bookingId).isPresent();
    }

    @Transactional
    public Feedback createFeedback(Long bookingId, Integer rating, String content) {
        Booking booking = bookingRepository.getReferenceById(bookingId);
        return feedbackRepository.save(new Feedback(booking, content, rating));
    }
}
