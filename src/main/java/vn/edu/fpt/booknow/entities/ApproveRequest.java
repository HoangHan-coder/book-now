package vn.edu.fpt.booknow.entities;

import lombok.Data;

@Data
public class ApproveRequest {
    private Long bookingId;
    private String reason;
}
