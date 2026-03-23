package vn.edu.fpt.booknow.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class FeedbackListDTO {

    private Long feedbackId;

    private Integer rating;

    private String customerName;

    private String roomName;

    private Boolean isHidden;

    private LocalDateTime createdAt;

}