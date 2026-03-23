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
import vn.edu.fpt.booknow.model.entities.ApproveRequest;
import vn.edu.fpt.booknow.model.entities.Booking;
import vn.edu.fpt.booknow.model.entities.BookingStatus;
import vn.edu.fpt.booknow.services.BookingService;
import vn.edu.fpt.booknow.services.BookingUpdateService;


import java.util.Random;

@Controller
@RequiredArgsConstructor
@RequestMapping(value = "/checkin")
public class CheckInController {
    private final CheckInHandler checkInHandler;
    private final SimpMessagingTemplate messagingTemplate;
    private final BookingService bookingService;
    private final BookingUpdateService bookingUpdateService;

    @GetMapping(value = "/booking-code")
    public String showBookingCodeForm(Model model) {
        return "customer/checkin_enter_code";
    }

    @GetMapping(value = "/success")
    public String checkInSuccess(Model model,@RequestParam(name = "bookingId")  Long bookingId) {
        Booking booking = bookingService.findById(bookingId);
        String roomCode = String.valueOf(100000 + new Random().nextInt(900000));
        model.addAttribute("booking", booking);
        model.addAttribute("roomCode", roomCode);
        return "customer/checkin_success";
    }

    @GetMapping(value = "/fail")
    public String checkInSuccess(Model model) {
        return "customer/checkin_fail";
    }

    @GetMapping(value = "/page")
    public String pageCheckin(@RequestParam (name = "code") String code, Model model) {
        System.out.println("code: " + code);
        Booking booking = bookingService.getBookingDetail(code);
        System.out.println("booking: " + booking);
        if (booking == null) {
            model.addAttribute("bookingCodeError", "Booking code does not exist");
            return "customer/checkin_enter_code";
        }
        if (booking.getBookingStatus().equals(BookingStatus.PENDING_PAYMENT)) {
            model.addAttribute("bookingStatusError1", "Booking has not paid yet");
            return "customer/checkin_enter_code";
        }

        if (booking.getBookingStatus().equals(BookingStatus.CHECKED_IN) || booking.getBookingStatus().equals(BookingStatus.CHECKED_OUT) || booking.getBookingStatus().equals(BookingStatus.COMPLETED)) {
            model.addAttribute("bookingStatusError2", "Booking has been checked in");
            return "customer/checkin_enter_code";
        }
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
        checkInHandler.approve(req.getBookingId(), req.getCheckInSessionId());

        return ResponseEntity.ok().build();
    }

    @PostMapping("/reject")
    public ResponseEntity<Void> reject(
            @RequestBody ApproveRequest req
    ) {
        Booking booking = bookingService.findById(req.getBookingId());
        bookingUpdateService.updateStatus(booking.getBookingCode(), BookingStatus.REJECTED_CHECKIN, req.getReason());
        checkInHandler.reject(req.getBookingId(),req.getCheckInSessionId());
        return ResponseEntity.ok().build();
    }

//    @GetMapping(value = "/bookinAdmin")
//    public String bookinAdmin(Model model) {
//
//        return "/admin/booking_update_status";
//    }




}
