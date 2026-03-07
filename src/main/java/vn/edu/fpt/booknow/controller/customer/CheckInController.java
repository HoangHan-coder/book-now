package vn.edu.fpt.booknow.controller.customer;


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
import vn.edu.fpt.booknow.entities.CheckInMessage;
import vn.edu.fpt.booknow.services.CheckInService;

@Controller
@RequiredArgsConstructor
@RequestMapping(value = "/checkin")
public class CheckInController {
    private final CheckInHandler checkInHandler;
    private final SimpMessagingTemplate messagingTemplate;

    @GetMapping(value = "/page")
    public String pageCheckin(@RequestParam (name = "id") int id, Model model) {
        model.addAttribute("id", id);
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
        checkInHandler.approve(req.getBookingId());
        return ResponseEntity.ok().build();
    }

    @GetMapping(value = "/bookinAdmin")
    public String bookinAdmin(Model model) {
        return "/admin/booking_update_status";
    }




}
