package vn.edu.fpt.booknow.conponents;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import vn.edu.fpt.booknow.model.entities.*;
import vn.edu.fpt.booknow.services.*;


import java.time.LocalDateTime;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class CheckInHandler {
    private final CloudinaryService cloudinaryService;
    private final SimpMessagingTemplate messagingTemplate;
//    private final CheckInService checkInService;
//    private final ObjectMapper jacksonObjectMapper;
//    private final BookingUpdateService bookingUpdateService;
    private final BookingService bookingService;
    private final CheckInSessionService checkInSessionService;




    public void startSession(Long bookingId, MultipartFile video) throws JsonProcessingException {
        CheckInMessage message = null;
        Map uploadVideo = cloudinaryService.uploadVideo(video);
        String videoUrl = uploadVideo.get("secure_url").toString();
        String videoUrlId = uploadVideo.get("public_id").toString();
        System.out.println("videoUrl: " + videoUrl);
        try {
            CheckInSession checkInSessionCheck = checkInSessionService.getCheckInSessionId(bookingId);
            CheckInSession checkInSession = checkInSessionService.updateCheckInSession(checkInSessionCheck.getCheckInSessionId(), CheckInSessionStatus.PENDING, videoUrl);
            message =
                    new CheckInMessage(checkInSession.getCheckInSessionId(),bookingId, videoUrl, "PENDING");
        } catch (Exception e) {
            CheckInSession checkInSession = checkInSessionService.createCheckInSession(bookingId, videoUrl, CheckInSessionStatus.PENDING, videoUrlId);
            message =
                    new CheckInMessage(checkInSession.getCheckInSessionId(),bookingId, videoUrl, "PENDING");
        }

        System.out.println("sending");
        messagingTemplate.convertAndSend(
                "/topic/checkin/admin", message
        );
        System.out.println("sent");
    }

    public void approve(Long bookingId, Long sessionId){
        String messageSuccess = "Check_in thành công";
        MessageSuccess mess = new MessageSuccess(messageSuccess, BookingStatus.CHECKED_IN);
        Booking booking = bookingService.findById(bookingId);
        LocalDateTime time = LocalDateTime.now();
        booking.setActualCheckInTime(time);
        checkInSessionService.updateCheckInSessionWhenApprove(sessionId,CheckInSessionStatus.APPROVED);
        // 5. Notify User
        messagingTemplate.convertAndSend(
                "/topic/checkin/user/" + bookingId,
                mess
        );
    }

    public void reject(Long bookingId, Long sessionId) {
        String note = bookingService.findById(bookingId).getNote();
        String messageSuccess = "REJECT do " + note + "vui lòng check_in lại";
        MessageSuccess mess = new MessageSuccess(messageSuccess, BookingStatus.REJECTED_CHECKIN);
        checkInSessionService.updateCheckInSessionWhenReject(sessionId,CheckInSessionStatus.REJECTED);
        // 5. Notify User
        messagingTemplate.convertAndSend(
                "/topic/checkin/user/" + bookingId,
                mess
        );
    }

}
