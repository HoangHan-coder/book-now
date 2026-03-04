package vn.edu.fpt.booknow.controllers;


import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import vn.edu.fpt.booknow.model.dto.MomoResponseDTO;
import vn.edu.fpt.booknow.services.MomoPaymentService;

@Controller
@RequestMapping("/pay")
public class PaymentController {

    private static final Logger log = LoggerFactory.getLogger(PaymentController.class);

    private final MomoPaymentService momoPaymentService;

    public PaymentController(MomoPaymentService momoPaymentService) {
        this.momoPaymentService = momoPaymentService;
    }

    @GetMapping("/payment")
    public String showPaymentForm() {
        return "payment";
    }

    @PostMapping("/create-payment")
    public String createPayment(
            @RequestParam("amount") long amount,
            @RequestParam("orderInfo") String orderInfo,
            Model model) {

        log.info("Nhận yêu cầu tạo thanh toán: amount={}, orderInfo={}", amount, orderInfo);
        try {
            if (amount <= 0) {
                model.addAttribute("error", "Số tiền phải lớn hơn 0");
                return "payment";
            }
            MomoResponseDTO response = momoPaymentService.createPayment(amount, orderInfo);
            if (response.isSuccess() && response.getPayUrl() != null) {
                log.info("Tạo thanh toán thành công, redirect đến: {}", response.getPayUrl());
                return "redirect:" + response.getPayUrl();
            } else {
                log.warn("MoMo trả về lỗi: resultCode={}, message={}", response.getResultCode(), response.getMessage());
                model.addAttribute("error", "Lỗi tạo thanh toán: " + response.getMessage());
                return "payment";
            }
        } catch (Exception e) {
            log.error("Exception khi tạo thanh toán", e);
            model.addAttribute("error", "Lỗi hệ thống: " + e.getMessage());
            return "payment";
        }
    }

    @GetMapping("/momo-return")
    public String handleReturn(
            @RequestParam(value = "partnerCode",  required = false) String partnerCode,
            @RequestParam(value = "requestId",    required = false) String requestId,
            @RequestParam(value = "orderId",      required = false) String orderId,
            @RequestParam(value = "amount",       required = false) String amount,
            @RequestParam(value = "orderInfo",    required = false) String orderInfo,
            @RequestParam(value = "orderType",    required = false) String orderType,
            @RequestParam(value = "transId",      required = false) String transId,
            @RequestParam(value = "message",      required = false) String message,
            @RequestParam(value = "localMessage", required = false) String localMessage,
            @RequestParam(value = "responseTime", required = false) String responseTime,
            @RequestParam(value = "resultCode",    required = false) String resultCode,
            @RequestParam(value = "payType",      required = false) String payType,
            @RequestParam(value = "extraData",    required = false, defaultValue = "") String extraData,
            @RequestParam(value = "signature",    required = false) String signature,
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
            model.addAttribute("success", false);
            model.addAttribute("message", "Lỗi xác thực chữ ký. Giao dịch không hợp lệ.");
            return "result";
        }

        boolean isSuccess = "0".equals(resultCode);
        model.addAttribute("success",      isSuccess);
        model.addAttribute("orderId",      orderId);
        model.addAttribute("amount",       amount);
        model.addAttribute("transId",      transId);
        model.addAttribute("orderInfo",    orderInfo);
        model.addAttribute("responseTime", responseTime);
        model.addAttribute("resultCode",    resultCode);
        model.addAttribute("message",      isSuccess ? "Thanh toán thành công!" : "Thanh toán thất bại: " + message);

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
                partnerCode  = getText(node, "partnerCode");
                requestId    = getText(node, "requestId");
                orderId      = getText(node, "orderId");
                amount       = getText(node, "amount");
                orderInfo    = getText(node, "orderInfo");
                orderType    = getText(node, "orderType");
                transId      = getText(node, "transId");
                message      = getText(node, "message");
                localMessage = getText(node, "localMessage");
                responseTime = getText(node, "responseTime");
                resultCode   = getText(node, "resultCode");
                payType      = getText(node, "payType");
                extraData    = node.has("extraData") ? node.get("extraData").asText("") : "";
                signature    = getText(node, "signature");
            } else {
                // Form params fallback
                partnerCode  = request.getParameter("partnerCode");
                requestId    = request.getParameter("requestId");
                orderId      = request.getParameter("orderId");
                amount       = request.getParameter("amount");
                orderInfo    = request.getParameter("orderInfo");
                orderType    = request.getParameter("orderType");
                transId      = request.getParameter("transId");
                message      = request.getParameter("message");
                localMessage = request.getParameter("localMessage");
                responseTime = request.getParameter("responseTime");
                resultCode   = request.getParameter("resultCode");
                payType      = request.getParameter("payType");
                extraData    = request.getParameter("extraData") != null ? request.getParameter("extraData") : "";
                signature    = request.getParameter("signature");
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

            if ("0".equals(resultCode)) {
                log.info("Thanh toán thành công: orderId={}, transId={}, amount={}", orderId, transId, amount);
            } else {
                log.warn("Thanh toán thất bại: orderId={}, resultCode={}", orderId, resultCode);
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
