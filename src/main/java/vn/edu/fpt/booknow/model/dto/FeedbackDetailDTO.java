package vn.edu.fpt.booknow.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FeedbackDetailDTO {
    private String customerName;
    private String avatarUrl;
    private Integer rating;
    private String content;
    private LocalDateTime createdAt;
    private Long bookingCount;
    private String replyContent;
    private LocalDateTime replyCreatedAt;
    private String managerName;
}