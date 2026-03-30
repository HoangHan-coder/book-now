package vn.edu.fpt.booknow.controllers.customer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.edu.fpt.booknow.model.dto.BookingDTO;
import vn.edu.fpt.booknow.model.dto.PaginatedResponse;
import vn.edu.fpt.booknow.model.dto.PaymentDTO;
import vn.edu.fpt.booknow.model.dto.RoomDTO;
import vn.edu.fpt.booknow.model.entities.*;
import vn.edu.fpt.booknow.services.*;

import java.util.List;

@Controller
@RequestMapping("/bookings")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private BookingListService bookingListService;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private FeedbackService feedbackService;

    @Autowired
    private JWTService jwtService;

    @GetMapping("/history")
    public String historyBookingView(Model model,
                                     @CookieValue(value = "Access_token", required = false) String token,
                                     @RequestParam(name = "page", defaultValue = "1") Integer page) {

        try {
            String email = token != null ? jwtService.extractUserName(token) : null;
            if (email == null) {
                System.out.println("Email bị null ..............");
                return "redirect:/auth/login";
            }
            System.out.println("Email là : " + email);
            Customer customer = customerService.findCusByEmail(email);
            if (customer == null) {
                System.out.println("KHông tìm thấy customer");
            }

            assert customer != null;
            System.out.println("customer là : " + customer.getFullName());

            PaginatedResponse<BookingDTO> paginatedBookings = bookingListService.bookingListWithPagination(page, email);

            if (paginatedBookings == null) {
                model.addAttribute("bookings", null);
                return  "booking-history";
            }

            System.out.println("KHông ai có lỗi ................");
            model.addAttribute("fullName", customer.getFullName());
            model.addAttribute("bookings", paginatedBookings.getData());
            model.addAttribute("currentPage", paginatedBookings.getCurrentPage());
            model.addAttribute("totalPages", paginatedBookings.getTotalPages());
            model.addAttribute("totalItems", paginatedBookings.getTotalItems());
            model.addAttribute("hasNext", paginatedBookings.isHasNext());
            model.addAttribute("hasPrevious", paginatedBookings.isHasPrevious());
            model.addAttribute("startIndex", paginatedBookings.getStartIndex());
            model.addAttribute("endIndex", paginatedBookings.getEndIndex());

            return "booking-history";
        } catch (Exception e) {
            return "redirect:/auth/login";
        }
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
            List<Payment> payments = paymentService.getPaymentByBookingId(booking);

            if (booking == null || !booking.getCustomer().getEmail().equals(email)) {
                return "redirect:/bookings/history";
            }

            Customer customer = customerService.findCusByEmail(email);

            model.addAttribute("fullName", customer.getFullName());
            model.addAttribute("booking", bookingDTO);
            model.addAttribute("room", new RoomDTO(room));
            model.addAttribute("payments", payments.stream().map(PaymentDTO::new).toList());
            model.addAttribute("hasFeedback", hasFeedback);
            return "booking-detail";
        } catch (Exception e) {
            return "redirect:/bookings/history";
        }
    }

    @PostMapping("/{id}")
    public String bookingHandleCancel(@PathVariable("id") String id,
                                      Model model) {
        try {
            Long bookingId = Long.parseLong(id);
            Booking booking = bookingService.getBookingById(bookingId);
            if (booking == null) {
                System.out.println("booking is null");
                return "redirect:/error/404";
            }

            bookingService.cancel(booking.getBookingId());
        } catch (NumberFormatException e) {
            System.out.println("Booking ID invalid");
            return "redirect:/error/404";
        }
        return "redirect:/bookings/" + id;
    }



    @GetMapping("/{id}/update-info")
    public String updateBookingInfoView(@PathVariable("id") String bookingIdRaw,
                                        @CookieValue(value = "Access_token", required = false) String token,
                                        Model model) {
        System.out.println(bookingIdRaw);
        try {
            Long bookingId = Long.parseLong(bookingIdRaw);
            System.out.println(bookingIdRaw);
            Booking booking = bookingService.getBookingById(bookingId);
            if (booking == null) {
                System.out.println("booking is null");
                return "redirect:/error/404";
            }
            String email = token != null ? jwtService.extractUserName(token) : null;
            if (email == null) return "redirect:/auth/login";

            Customer customer = customerService.findCusByEmail(email);

            model.addAttribute("fullName", customer.getFullName());
            model.addAttribute("booking", new BookingDTO(booking));
        } catch (Exception e) {
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
                System.out.println(e.getMessage() + " 148");
                model.addAttribute("error", e.getMessage());
                return "redirect:/error/404";
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
