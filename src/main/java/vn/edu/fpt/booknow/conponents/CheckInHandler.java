package vn.edu.fpt.booknow.conponents;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import vn.edu.fpt.booknow.entities.Booking;
import vn.edu.fpt.booknow.entities.BookingStatus;
import vn.edu.fpt.booknow.entities.CheckInMessage;
import vn.edu.fpt.booknow.entities.MessageSuccess;
import vn.edu.fpt.booknow.repositories.BookingRepository;
import vn.edu.fpt.booknow.services.CheckInService;
import vn.edu.fpt.booknow.services.CloudinaryService;
import vn.edu.fpt.booknow.services.customer.BookingService;
import vn.edu.fpt.booknow.services.staff.BookingUpdateService;

@Component
@RequiredArgsConstructor
public class CheckInHandler {
    private final CloudinaryService cloudinaryService;
    private final SimpMessagingTemplate messagingTemplate;
    private final CheckInService checkInService;
    private final ObjectMapper jacksonObjectMapper;
    private final BookingUpdateService bookingUpdateService;
    private final BookingService bookingService;


    public void startSession(Long bookingId, MultipartFile video) throws JsonProcessingException {
        String videoUrl = cloudinaryService.uploadVideo(video);
        System.out.println("videoUrl: " + videoUrl);

        CheckInMessage message =
                new CheckInMessage(bookingId, videoUrl, "PENDING");


        System.out.println("sending");
        messagingTemplate.convertAndSend(
                "/topic/checkin/admin", message
        );
        System.out.println("sent");
    }

    public void approve(Long bookingId) {
        String messageSuccess = "Check_in thành công";
        MessageSuccess mess = new MessageSuccess(messageSuccess);
        // 5. Notify User
        messagingTemplate.convertAndSend(
                "/topic/checkin/user/" + bookingId,
                mess
        );
    }

    public void reject(Long bookingId) {
        String note = bookingService.findById(bookingId).getNote();
        String messageSuccess = "REJECT do " + note + "vui lòng check_in lại";
        MessageSuccess mess = new MessageSuccess(messageSuccess);
        // 5. Notify User
        messagingTemplate.convertAndSend(
                "/topic/checkin/user/" + bookingId,
                mess
        );
    }

}
