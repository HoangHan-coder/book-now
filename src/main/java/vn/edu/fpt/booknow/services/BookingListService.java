package vn.edu.fpt.booknow.services;

import org.springframework.stereotype.Service;
import vn.edu.fpt.booknow.model.dto.BookingDTO;
import vn.edu.fpt.booknow.model.dto.PaginatedResponse;
import vn.edu.fpt.booknow.model.entities.Booking;
import vn.edu.fpt.booknow.model.entities.BookingStatus;
import vn.edu.fpt.booknow.repositories.BookingRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class BookingListService {

    private final BookingRepository bookingRepository;

    public BookingListService(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    public List<Booking> getAllBooking() {
        return bookingRepository.findAllWithCustomer();
    }
    public List<Booking> filter(
            String fromDate,
            String toDate,
            BookingStatus status,
            String keyword
    ) {

        final LocalDateTime from =
                (fromDate != null && !fromDate.isBlank())
                        ? LocalDate.parse(fromDate).atStartOfDay()
                        : null;

        final LocalDateTime to =
                (toDate != null && !toDate.isBlank())
                        ? LocalDate.parse(toDate).atTime(23, 59, 59)
                        : null;

        final String search =
                (keyword != null && !keyword.isBlank())
                        ? keyword.trim().toLowerCase()
                        : null;

        return bookingRepository.findAllWithCustomer().stream()
                .filter(b -> {

                    // 1️⃣ Check-in
                    if (from != null && b.getCheckInTime().isBefore(from)) {
                        return false;
                    }

                    // 2️⃣ Check-out
                    if (to != null && b.getCheckOutTime().isAfter(to)) {
                        return false;
                    }

                    // 3️⃣ Status
                    if (status != null && b.getBookingStatus() != status) {
                        return false;
                    }

                    // 4️⃣ Keyword
                    if (search != null) {

                        boolean matchCode =
                                b.getBookingCode() != null &&
                                        b.getBookingCode().toLowerCase().contains(search);

                        boolean matchName =
                                b.getCustomer() != null &&
                                        b.getCustomer().getFullName() != null &&
                                        b.getCustomer().getFullName().toLowerCase().contains(search);

                        if (!matchCode && !matchName) {
                            return false;
                        }
                    }

                    return true;
                })
                .toList();
    }

    /**
     * Filter and paginate bookings
     * @param fromDate Check-in date (optional)
     * @param toDate Check-out date (optional)
     * @param status Booking status (optional)
     * @param keyword Search keyword (optional)
     * @param page Page number (1-indexed, auto-adjusted if invalid)
     * @return PaginatedResponse containing page data and pagination info
     */
    public PaginatedResponse<Booking> filterWithPagination(
            String fromDate,
            String toDate,
            BookingStatus status,
            String keyword,
            int page
    ) {
        final int PAGE_SIZE = 10;

        // 1️⃣ Get all filtered data
        List<Booking> allFilteredBookings = filter(fromDate, toDate, status, keyword);

        // 2️⃣ Calculate pagination metrics
        long totalItems = allFilteredBookings.size();
        int totalPages = (int) Math.ceil((double) totalItems / PAGE_SIZE);

        // 3️⃣ Validate page number
        if (totalPages == 0) {
            totalPages = 1;
        }
        if (page < 1) {
            page = 1;
        }
        if (page > totalPages) {
            page = totalPages;
        }

        // 4️⃣ Slice data for current page
        int startIndex = (page - 1) * PAGE_SIZE;
        int endIndex = Math.min(startIndex + PAGE_SIZE, (int) totalItems);
        List<Booking> pageData = allFilteredBookings.subList(startIndex, endIndex);

        // 5️⃣ Return paginated response
        return new PaginatedResponse<>(pageData, page, totalPages, totalItems, PAGE_SIZE);
    }

    public PaginatedResponse<BookingDTO> bookingListWithPagination(int page, String email) {
        List<Booking> bookingList = bookingRepository.getBookingByCustomer_Email(email).orElse(null);

        if (bookingList == null) {
            System.out.println("customer có email" + email + "không có booking nào đã đặt");
            return null;
        }
        List<BookingDTO> bookings = bookingList.stream().map(BookingDTO::new).toList();
        System.out.println("Đã map thành công list booking thành list bookingDTO.");
        final int PAGE_SIZE = 6;
        long totalItems = bookings.size();
        int totalPages = (int) Math.ceil((double) totalItems / PAGE_SIZE);

        if (page < 1) {
            page = 1;
        }
        if (page > totalPages) {
            page = totalPages;
        }

        // Slice data for current page
        int startIndex = (page - 1) * PAGE_SIZE;
        int endIndex = Math.min(startIndex + PAGE_SIZE, (int) totalItems);
        try {
            List<BookingDTO> pageData = bookings.subList(startIndex, endIndex);
            System.out.println("Return list bookingDTO đã split rồi nhé.......");
            return new PaginatedResponse<>(pageData, page, totalPages, totalItems, PAGE_SIZE, startIndex, endIndex);
        } catch (Exception e) {
            System.out.println("list bookingDTO bị lỏ ròi bạn ơi.........");
            return null;
        }

    }
}