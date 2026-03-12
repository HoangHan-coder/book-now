package vn.edu.fpt.booknow.controllers.customer;


import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vn.edu.fpt.booknow.conponents.CheckInHandler;
import vn.edu.fpt.booknow.entities.ApproveRequest;
import vn.edu.fpt.booknow.entities.Booking;
import vn.edu.fpt.booknow.entities.BookingStatus;
import vn.edu.fpt.booknow.services.customer.BookingService;
import vn.edu.fpt.booknow.services.staff.BookingUpdateService;

@Controller
@RequiredArgsConstructor
@RequestMapping(value = "/checkin")
public class CheckInController {
    private final CheckInHandler checkInHandler;
    private final SimpMessagingTemplate messagingTemplate;
    private final BookingService bookingService;
    private final BookingUpdateService bookingUpdateService;

    @GetMapping(value = "/page")
    public String pageCheckin(@RequestParam (name = "code") String code, Model model) {
        Booking booking = bookingService.getBookingDetail(code);
        model.addAttribute("booking", booking);
        return "/customer/check_in";
    }

    @PostMapping("/start")
    public ResponseEntity<Void> start(
            @RequestParam Long bookingId,
            @RequestParam MultipartFile video
    ) throws JsonProcessingException {
        System.out.println("running");
        checkInHandler.startSession(bookingId, video);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/approve")
    public ResponseEntity<Void> approve(
            @RequestBody ApproveRequest req
    ) {
        Booking booking = bookingService.findById(req.getBookingId());
        bookingUpdateService.updateStatus(booking.getBookingCode(), BookingStatus.CHECKED_IN, null);
        checkInHandler.approve(req.getBookingId());

        return ResponseEntity.ok().build();
    }

    @PostMapping("/reject")
    public ResponseEntity<Void> reject(
            @RequestBody ApproveRequest req
    ) {
        Booking booking = bookingService.findById(req.getBookingId());
        bookingUpdateService.updateStatus(booking.getBookingCode(), BookingStatus.REJECT, req.getReason());
        checkInHandler.reject(req.getBookingId());
        return ResponseEntity.ok().build();
    }

//    @GetMapping(value = "/bookinAdmin")
//    public String bookinAdmin(Model model) {
//
//        return "/admin/booking_update_status";
//    }




}
