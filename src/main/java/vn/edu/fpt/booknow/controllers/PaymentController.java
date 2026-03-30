package vn.edu.fpt.booknow.controllers;


import jakarta.servlet.http.HttpServletRequest;
import org.apache.poi.ss.formula.functions.T;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.edu.fpt.booknow.model.dto.BookingDTO;
import vn.edu.fpt.booknow.model.dto.MomoResponseDTO;
import vn.edu.fpt.booknow.model.entities.*;
import vn.edu.fpt.booknow.services.BookingService;
import vn.edu.fpt.booknow.services.HousekeepingTaskService;
import vn.edu.fpt.booknow.services.MomoPaymentService;
import vn.edu.fpt.booknow.services.PaymentService;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Controller
@RequestMapping("/pay")
public class PaymentController {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private HousekeepingTaskService housekeepingTaskService;

    @Autowired
    private PaymentService paymentService;

    private static final Logger log = LoggerFactory.getLogger(PaymentController.class);

    private final MomoPaymentService momoPaymentService;

    public PaymentController(MomoPaymentService momoPaymentService) {
        this.momoPaymentService = momoPaymentService;
    }



    @PostMapping("/create-payment")
    public String createPayment(
            @RequestParam("bookingId") String bookingIdRaw,
            @RequestParam(value = "timetableId", required = false) String timetableIdRaw,
            RedirectAttributes redirectAttributes) {
        try {
            Long bookingId = Long.parseLong(bookingIdRaw);

            MomoResponseDTO response;

            Booking booking = bookingService.getBookingById(bookingId);

            if (booking == null) {
                redirectAttributes.addFlashAttribute("error", "Lỗi hệ thống: Không tìm thấy booking");
                return "redirect:/bookings/" + bookingId;
            }

            BookingDTO bookingDTO = new BookingDTO(booking);

            if (bookingDTO.isOverStay()) {

                if (timetableIdRaw == null) {
                    return "error/500";
                }

                Long timetableId = Long.parseLong(timetableIdRaw);
                response = payForExtendBooking(bookingId, timetableId);
            } else {
                response = payForNewBooking(bookingId);
            }


            if (response.isSuccess() && response.getPayUrl() != null) {
                log.info("Tạo thanh toán thành công, redirect đến: {}", response.getPayUrl());
                return "redirect:" + response.getPayUrl();
            } else {
                log.warn("MoMo trả về lỗi: resultCode={}, message={}", response.getResultCode(), response.getMessage());
                redirectAttributes.addFlashAttribute("error", "Lỗi tạo thanh toán: " + response.getMessage());
                return "redirect:/bookings/" + bookingId;
            }
        } catch (Exception e) {
            log.error("Exception khi tạo thanh toán", e);
            redirectAttributes.addFlashAttribute("error", "Lỗi hệ thống: " + e.getMessage());
            return "redirect:/bookings/" + bookingIdRaw;
        }
    }

    private MomoResponseDTO payForNewBooking(Long bookingId) throws Exception {
        Booking booking = bookingService.getBookingById(bookingId);
        if (booking == null) {
            throw new Exception("Booking not exist");
        }
        return momoPaymentService.createPayment(booking.getTotalAmount().longValue(), booking.getBookingCode());
    }

    private MomoResponseDTO payForExtendBooking(Long bookingId, Long timetableId) throws Exception {
        Booking booking = bookingService.getBookingById(bookingId);
        if (booking == null) {
            throw new Exception("Booking not exist");
        }

        if (timetableId < 1 || timetableId > 4) {
            throw new Exception("Timetable invalid");
        }

        RoomType roomType = booking.getRoom().getRoomType();
        if (roomType == null) {
            throw new Exception("RoomType invalid");
        }
        if (timetableId == 4) {
            return momoPaymentService.createPayment(roomType.getOverPrice().longValue(), booking.getBookingCode());
        }

        return momoPaymentService.createPayment(roomType.getBasePrice().longValue(), booking.getBookingCode());
    }

    @GetMapping("/momo-return")
    public String handleReturn(
            @RequestParam(value = "partnerCode", required = false) String partnerCode,
            @RequestParam(value = "requestId", required = false) String requestId,
            @RequestParam(value = "orderId", required = false) String orderId,
            @RequestParam(value = "amount", required = false) String amount,
            @RequestParam(value = "orderInfo", required = false) String orderInfo,
            @RequestParam(value = "orderType", required = false) String orderType,
            @RequestParam(value = "transId", required = false) String transId,
            @RequestParam(value = "message", required = false) String message,
            @RequestParam(value = "localMessage", required = false) String localMessage,
            @RequestParam(value = "responseTime", required = false) String responseTime,
            @RequestParam(value = "resultCode", required = false) String resultCode,
            @RequestParam(value = "payType", required = false) String payType,
            @RequestParam(value = "extraData", required = false, defaultValue = "") String extraData,
            @RequestParam(value = "signature", required = false) String signature,
            Model model) {

        log.info("=== Nhận callback returnUrl ===");
        log.info("OrderId: {}, resultCode: {}, TransId: {}", orderId, resultCode, transId);

        boolean isValid = momoPaymentService.verifyReturnSignature(
                partnerCode, requestId, orderId, amount, orderInfo, orderType,
                transId, message, responseTime, resultCode,
                payType, extraData, signature
        );

        if (!isValid) {
            log.warn("Signature không hợp lệ từ returnUrl!");
            model.addAttribute("message", "Lỗi xác thực chữ ký. Giao dịch không hợp lệ.");
            return "result";
        }

        boolean isSuccess = "0".equals(resultCode);

        Booking booking = bookingService.getBookingDetail(orderInfo);

        if (booking == null) {
            log.warn("Signature không hợp lệ từ returnUrl!");
            model.addAttribute("message", "Lỗi booking không tồn tại!");
            return "result";
        }

        if (booking.getBookingStatus() == BookingStatus.FAILED && isSuccess) {
            model.addAttribute("message", "Thanh toán thành công trên MoMo, nhưng ĐƠN HÀNG ĐÃ HẾT HẠN (quá 15 phút) trên hệ thống. Vui lòng liên hệ Hotline để được hỗ trợ hoàn tiền.");
            isSuccess = false; // Đổi cờ để nó hiện giao diện màu đỏ cảnh báo thay vì xanh
            log.error("CẢNH BÁO (Return): Khách toán đơn {} quá hạn, số tiền: {}", orderInfo, amount);
        } else if (booking.getBookingStatus() != BookingStatus.PAID) {

            BigDecimal total = BigDecimal.valueOf(Double.parseDouble(amount));
            if (isSuccess) {
                bookingService.updateStatus(BookingStatus.PAID, orderInfo);
                paymentService.creatPayment(new Payment(booking, total, "MOMO Payment", "SUCCESS"));
            } else {
                bookingService.updateStatus(BookingStatus.PENDING_PAYMENT, orderInfo);
                paymentService.creatPayment(new Payment(booking, total, "MOMO Payment", "FAILED"));
            }
        }
        model.addAttribute("bookingId", booking.getBookingId());
        model.addAttribute("success", isSuccess);
        model.addAttribute("orderId", orderId);
        model.addAttribute("amount", amount);
        model.addAttribute("transId", transId);
        model.addAttribute("orderInfo", orderInfo);
        model.addAttribute("responseTime", responseTime);
        model.addAttribute("resultCode", resultCode);
        model.addAttribute("message", isSuccess ? "Thanh toán thành công!" : "Thanh toán thất bại: " + message);
        log.info("Kết quả thanh toán: success={}, transId={}", isSuccess, transId);
        return "result";
    }

    @PostMapping("/momo-notify")
    @ResponseBody
    public String handleNotify(@RequestBody(required = false) String rawBody,
                               HttpServletRequest request) {

        log.info("=== Nhận IPN Notify từ MoMo ===");

        try {
            // MoMo có thể gửi JSON hoặc form-encoded, xử lý cả 2
            String contentType = request.getContentType();
            log.debug("Content-Type: {}", contentType);
            log.debug("Raw body: {}", rawBody);

            String partnerCode, requestId, orderId, amount, orderInfo, orderType;
            String transId, message, localMessage, responseTime, resultCode;
            String payType, extraData, signature;

            if (rawBody != null && rawBody.trim().startsWith("{")) {
                // JSON body
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                com.fasterxml.jackson.databind.JsonNode node = mapper.readTree(rawBody);
                partnerCode = getText(node, "partnerCode");
                requestId = getText(node, "requestId");
                orderId = getText(node, "orderId");
                amount = getText(node, "amount");
                orderInfo = getText(node, "orderInfo");
                orderType = getText(node, "orderType");
                transId = getText(node, "transId");
                message = getText(node, "message");
                localMessage = getText(node, "localMessage");
                responseTime = getText(node, "responseTime");
                resultCode = getText(node, "resultCode");
                payType = getText(node, "payType");
                extraData = node.has("extraData") ? node.get("extraData").asText("") : "";
                signature = getText(node, "signature");
            } else {
                // Form params fallback
                partnerCode = request.getParameter("partnerCode");
                requestId = request.getParameter("requestId");
                orderId = request.getParameter("orderId");
                amount = request.getParameter("amount");
                orderInfo = request.getParameter("orderInfo");
                orderType = request.getParameter("orderType");
                transId = request.getParameter("transId");
                message = request.getParameter("message");
                localMessage = request.getParameter("localMessage");
                responseTime = request.getParameter("responseTime");
                resultCode = request.getParameter("resultCode");
                payType = request.getParameter("payType");
                extraData = request.getParameter("extraData") != null ? request.getParameter("extraData") : "";
                signature = request.getParameter("signature");
            }

            log.info("OrderId: {}, ResultCode: {}, TransId: {}, Amount: {}",
                    orderId, resultCode, transId, amount);

            if (signature == null) {
                log.error("Không có signature!");
                return "INVALID_SIGNATURE";
            }

            boolean isValid = momoPaymentService.verifyNotifySignature(
                    partnerCode, requestId, orderId, amount, orderInfo, orderType,
                    transId, message, responseTime, resultCode,
                    payType, extraData, signature
            );

            if (!isValid) {
                log.error("Signature IPN không hợp lệ!");
                return "INVALID_SIGNATURE";
            }

            Booking booking = bookingService.getBookingDetail(orderInfo);
            if (booking == null) {
                return "INVALID_ORDER_INFO";
            }

            if ("0".equals(resultCode)) {
                log.info("Thanh toán thành công: orderId={}, transId={}, amount={}", orderId, transId, amount);
                if (booking.getBookingStatus() == BookingStatus.FAILED) {
                    log.error("KHẨN CẤP: Giao dịch thành công nhưng đơn hàng đã quá hạn. Cần liên hệ hỗ trợ hoàn tiền! Amount: {}", amount);
                    booking.setNote("KHẨN CẤP: Giao dịch thành công nhưng đơn hàng đã quá hạn. Cần liên hệ hỗ trợ hoàn tiền! Amount: " + amount);
                    bookingService.save(booking);
                } else if (booking.getBookingStatus() == BookingStatus.PAID) {
                    log.info("Đơn hàng đã được đánh dấu PAID trước đó, bỏ qua.");
                } else {
                    assert amount != null;
                    BigDecimal total = BigDecimal.valueOf(Double.parseDouble(amount));
                    bookingService.updateStatus(BookingStatus.PAID, orderInfo);
                    paymentService.creatPayment(new Payment(
                            booking,
                            total,
                            "MOMO Payment",
                            "SUCCESS"
                    ));
                    HousekeepingTask housekeepingTask = housekeepingTaskService.getByBooKingCode(orderInfo);
                    if (housekeepingTask == null) {
                        HousekeepingTask task = housekeepingTaskService.newTask(new HousekeepingTask(booking.getRoom(),
                                TaskStatus.PENDING,
                                PriorityStatus.NORMAL,
                                booking,
                                LocalDateTime.now(),
                                "ClEANING",
                                "Create by the system"));
                        if (task == null) {
                            log.error("Lỗi xử lý tạo task tự động");
                        }
                    }

                }
            } else {
                log.warn("Thanh toán thất bại: orderId={}, resultCode={}", orderId, resultCode);
                assert amount != null;
                BigDecimal total = BigDecimal.valueOf(Double.parseDouble(amount));
                bookingService.updateStatus(BookingStatus.FAILED, orderInfo);
                paymentService.creatPayment(new Payment(
                        booking,
                        total,
                        "MOMO Payment",
                        "FAILED"
                ));
            }
            return "0";

        } catch (Exception e) {
            log.error("Lỗi xử lý IPN: {}", e.getMessage(), e);
            return "ERROR";
        }
    }

    private String getText(com.fasterxml.jackson.databind.JsonNode node, String field) {
        return node.has(field) && !node.get(field).isNull() ? node.get(field).asText() : null;
    }
}
