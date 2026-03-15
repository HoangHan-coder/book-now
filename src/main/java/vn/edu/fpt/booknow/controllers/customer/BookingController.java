package vn.edu.fpt.booknow.controllers.customer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.edu.fpt.booknow.model.dto.BookingDTO;
import vn.edu.fpt.booknow.model.dto.PaymentDTO;
import vn.edu.fpt.booknow.model.dto.RoomDTO;
import vn.edu.fpt.booknow.model.entities.*;
import vn.edu.fpt.booknow.services.BookingService;
import vn.edu.fpt.booknow.services.FeedbackService;
import vn.edu.fpt.booknow.services.JWTService;

import java.util.List;

import vn.edu.fpt.booknow.services.PaymentService;

@Controller
@RequestMapping("/bookings")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private FeedbackService feedbackService;

    @Autowired
    private JWTService jwtService;

    @GetMapping("/history")
    public String historyBookingView(Model model,
                                     @CookieValue(value = "Access_token", required = false) String token) {

        String email = token != null ? jwtService.extractUserName(token) : null;
        if (email == null)
            return "redirect:/auth/login";

        List<Booking> bookings = bookingService.getBookingByEmail(email);

        List<BookingDTO> bookingDTOList = bookings.stream().map(BookingDTO::new).toList();

        model.addAttribute("bookings", bookingDTOList);
        return "booking-history";
    }

    @GetMapping("/{id}")
    public String bookingDetailView(Model model,
                                    @CookieValue(value = "Access_token", required = false) String token,
                                    @PathVariable("id") String bookingId) {


        try {
            Long id = Long.parseLong(bookingId);
            String email = token != null ? jwtService.extractUserName(token) : null;
            boolean hasFeedback = feedbackService.hasFeedback(id);
            if (email == null) return "redirect:/auth/login";

            Booking booking = bookingService.getBookingById(id);
            BookingDTO bookingDTO = new BookingDTO(booking);
            Room room = booking.getRoom();
            Payment payment = paymentService.getPaymentByBookingId(booking);
            System.out.println(payment.getPaymentId());
            if (booking == null || !booking.getCustomer().getEmail().equals(email)) {
                return "redirect:/bookings/history";
            }
            model.addAttribute("booking", bookingDTO);
            model.addAttribute("room", new RoomDTO(room));
            model.addAttribute("payment", new PaymentDTO(payment));
            model.addAttribute("hasFeedback", hasFeedback);
            return "booking-detail";
        } catch (NumberFormatException e) {
            return "redirect:/bookings/history";
        }
    }

    @GetMapping("/{id}/update-info")
    public String updateBookingInfoView(@PathVariable("id") String id,
                                        Model model) {
        try {
            Long bookingId = Long.parseLong(id);
            Booking booking = bookingService.getBookingById(bookingId);
            if (booking == null) {
                System.out.println("booking is null");
                return "redirect:/error/404";
            }

            model.addAttribute("booking", new BookingDTO(booking));
        } catch (NumberFormatException e) {
            System.out.println("Booking ID invalid");
            return "redirect:/error/404";
        }


        return "booking-update-info";
    }

    @PostMapping("/{id}/update-info")
    public String updateBookingInfo(@PathVariable("id") String id,
                                    @RequestParam("idCardFront") MultipartFile idCardFront,
                                    @RequestParam("idCardBack") MultipartFile idCardBack,
                                    Model model) {
        try {
            Long bookingId = Long.parseLong(id);

            try {
                bookingService.updateIdCard(idCardFront, idCardBack, bookingId);
                return "redirect:/bookings/" + bookingId;
            } catch (Exception e) {
                model.addAttribute("error", e.getMessage());
                return "booking-update-info";
            }

        } catch (NumberFormatException e) {
            return "redirect:/error/404";
        }

    }

    @GetMapping("/{id}/feedback")
    public String feedbackForm(@PathVariable("id") String id,
                               @RequestParam(value = "error", required = false) String error,
                               Model model) {
        try {
            Long bookingId = Long.parseLong(id);
            Booking booking = bookingService.getBookingById(bookingId);

            if (error != null && !error.isBlank()) {
                model.addAttribute("error", error);
            }

            model.addAttribute("booking", new BookingDTO(booking));
            model.addAttribute("room", new RoomDTO(booking.getRoom()));
            return "feedback_form";
        } catch (Exception e) {
            return "error/404";
        }
    }

    @PostMapping("/{id}/feedback")
    public String feedbackHandle(@PathVariable("id") String id,
                                 @RequestParam(value = "rating", required = false) String ratingRaw,
                                 @RequestParam(value = "comment", required = false) String content,
                                 @RequestParam(value = "bookingCode", required = false) String bookingCode,
                                 RedirectAttributes redirectAttributes) {
        try {
            Long bookingId = Long.parseLong(id);

            if (ratingRaw == null || ratingRaw.isBlank()) {
                System.out.println("Bạn chưa chọn số sao.");
                redirectAttributes.addFlashAttribute("error", "Bạn chưa chọn số sao.");
                return "redirect:/bookings/" + bookingId + "/feedback";
            }

            if (content == null || content.isBlank()) {
                System.out.println("Nội dung feedback ko được bỏ trống.");
                redirectAttributes.addFlashAttribute("error", "Nội dung feedback ko được bỏ trống.");
                return "redirect:/bookings/" + bookingId + "/feedback";
            }

            Integer rating = Integer.parseInt(ratingRaw);
            Feedback feedback = feedbackService.createFeedback(bookingId, rating, content);

            if (feedback == null) {
                return "error/500";
            }
            bookingService.updateStatus(BookingStatus.COMPLETED, bookingCode);
            return "redirect:/bookings/" + bookingId;

        } catch (Exception e) {
            System.out.println(e.getMessage());
            return "error/404";
        }

    }


}
