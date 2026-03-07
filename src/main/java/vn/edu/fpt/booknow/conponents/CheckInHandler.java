package vn.edu.fpt.booknow.conponents;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import vn.edu.fpt.booknow.entities.Booking;
import vn.edu.fpt.booknow.entities.CheckInMessage;
import vn.edu.fpt.booknow.repositories.BookingRepository;
import vn.edu.fpt.booknow.services.CheckInService;
import vn.edu.fpt.booknow.services.CloudinaryService;

@Component
@RequiredArgsConstructor
public class CheckInHandler {
    private final CloudinaryService cloudinaryService;
    private final SimpMessagingTemplate messagingTemplate;
    private final CheckInService checkInService;
    private final ObjectMapper jacksonObjectMapper;

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

        // 4. Update booking
        Booking booking = checkInService.findById(bookingId);
        booking.setBookingStatus("CHECKED_IN");
        checkInService.save(booking);

        // 5. Notify User
        messagingTemplate.convertAndSend(
                "/topic/checkin/user/" + bookingId,
                "CHECKED_IN"
        );
    }

}
