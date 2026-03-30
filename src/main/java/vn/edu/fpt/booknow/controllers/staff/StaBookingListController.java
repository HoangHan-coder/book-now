package vn.edu.fpt.booknow.controllers.staff;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import vn.edu.fpt.booknow.model.dto.PaginatedResponse;
import vn.edu.fpt.booknow.model.entities.Booking;
import vn.edu.fpt.booknow.model.entities.BookingStatus;
import vn.edu.fpt.booknow.services.BookingListService;

import java.time.LocalDate;

@Controller
@RequestMapping("/staff")
public class StaBookingListController {

    private final BookingListService bookingListService;
    public StaBookingListController(BookingListService bookingListService) {
        this.bookingListService = bookingListService;
    }
    @GetMapping("/bookings")
    public String listBookings(
            @RequestParam(name = "checkIn", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate checkIn,

            @RequestParam(name = "checkOut", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate checkOut,

            @RequestParam(name = "status", required = false)
            BookingStatus status,

            @RequestParam(name = "keyword", required = false)
            String keyword,

            @RequestParam(name = "page", defaultValue = "1")
            int page,

            Model model
    ) {

        PaginatedResponse<Booking> paginatedBookings = bookingListService.filterWithPagination(
                checkIn != null ? checkIn.toString() : null,
                checkOut != null ? checkOut.toString() : null,
                status,
                keyword,
                page
        );

        model.addAttribute("bookings", paginatedBookings.getData());
        model.addAttribute("currentPage", paginatedBookings.getCurrentPage());
        model.addAttribute("totalPages", paginatedBookings.getTotalPages());
        model.addAttribute("totalItems", paginatedBookings.getTotalItems());
        model.addAttribute("hasNext", paginatedBookings.isHasNext());
        model.addAttribute("hasPrevious", paginatedBookings.isHasPrevious());

        return "private/staff-booking-list";
    }
}
