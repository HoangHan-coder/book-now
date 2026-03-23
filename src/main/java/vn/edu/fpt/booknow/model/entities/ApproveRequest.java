package vn.edu.fpt.booknow.model.entities;

import lombok.Data;

@Data
public class ApproveRequest {
    private Long checkInSessionId;
    private Long bookingId;
    private String reason;
    private Long taskId;
}
