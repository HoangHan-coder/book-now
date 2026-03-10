package vn.edu.fpt.booknow.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FeedbackStatisticsDTO {
    private Double averageRating;
    private Long totalReviews;
    private Map<Integer, Long> ratingCounts; // Map<Số sao, Số lượng>
}
