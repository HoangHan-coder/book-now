package vn.edu.fpt.booknow.controllers;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.edu.fpt.booknow.model.dto.ForgotPasswordRequest;
import vn.edu.fpt.booknow.model.dto.ResetPasswordRequest;
import vn.edu.fpt.booknow.model.dto.VerifyOtpRequest;
import vn.edu.fpt.booknow.model.entities.Customer;
import vn.edu.fpt.booknow.model.entities.StaffAccount;
import vn.edu.fpt.booknow.services.JWTService;
import vn.edu.fpt.booknow.services.OTPService;
import vn.edu.fpt.booknow.services.CustomerService;
import vn.edu.fpt.booknow.services.StaffAccountService;
import vn.edu.fpt.booknow.services.MailService;
import vn.edu.fpt.booknow.services.RecaptchaService;
import vn.edu.fpt.booknow.services.CustomerService;

import java.util.Objects;
import java.util.regex.Pattern;


@Controller
public class AuthController {

    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
    private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);
    private static final int MIN_PASSWORD_LENGTH = 8;

    private final OTPService otpService;

    private final PasswordEncoder passwordEncoder;

    private final  CustomerService customerService;

    private final JWTService jwtService;

    private final StaffAccountService staffAccountService;

    @Autowired
    public AuthController(OTPService otpService, PasswordEncoder passwordEncoder,
                          CustomerService customerService, JWTService jwtService,
                          StaffAccountService staffAccountService) {
        this.otpService = otpService;
        this.passwordEncoder = passwordEncoder;
        this.customerService = customerService;
        this.jwtService = jwtService;
        this.staffAccountService = staffAccountService;
    }

    @GetMapping("/admin/login")
    public String loginAdminPanel(Model model,
                                  @RequestParam(name = "error", required = false) String error,
                                  @RequestParam(name = "captchaError", required = false) String captchaError) {
        if (error != null) {
            model.addAttribute("error", "Email hoặc mật khẩu  không chính xác!");
        }
        if (captchaError != null) {
            model.addAttribute("captchaError", "Captcha không hợp lệ!");
        }
        model.addAttribute("staffAccount", new StaffAccount());
        return "public/authentication/login-admin";
    }

    @PostMapping("/admin/login")
    public String loginAdminHandle(@ModelAttribute StaffAccount staffAccount,
                                   HttpServletResponse response) {
        boolean loginStatus = staffAccountService.verify(staffAccount, response);
        if (!loginStatus) {
            return  "redirect:/admin/login?error";
        }

        StaffAccount account = staffAccountService.getAccount(staffAccount.getEmail());

        if (account == null) {
            return  "redirect:/admin/login?error";
        }

        switch (account.getRole()) {
            case "ADMIN" -> {
                return "redirect:/admin/dashboard";
            }
            case "STAFF" -> {
                return "redirect:/staff/dashboard";
            }
            case "HOUSEKEEPING" -> {
                return "redirect:/housekeeping/task";
            }
            default -> {
                return  "redirect:/admin/login?error";
            }
        }
    }

    @GetMapping("/auth/login")
    public String loginCustomerPanel(Model model,
                                     @RequestParam(name = "error", required = false) String error,
                                     @RequestParam(name = "captchaError", required = false) String captchaError) {
        if (error != null) {
            model.addAttribute("error", "Email hoặc mật khẩu  không chính xác!");
        }
        if (captchaError != null) {
            model.addAttribute("captchaError", "Captcha không hợp lệ!");
        }

        model.addAttribute("customer", new Customer());

        return "public/authentication/login-customer";
    }

    @PostMapping("/auth/login")
    public String loginCustomerHandle(@RequestParam(name = "g-recaptcha-response", required = false) String recaptchaResponse,
                                      @ModelAttribute Customer customer,
                                      HttpServletResponse response) {
        boolean loginStatus = customerService.verify(customer, response);
        System.out.println("login status: " + loginStatus);
        if (!loginStatus) {
            return  "redirect:/auth/login?error";
        }
        return "redirect:/home";
    }

    @GetMapping("auth/logout")
    public String logoutCustomerHandle(HttpServletResponse response) {
        jwtService.removeCookie(response);
        return "redirect:/auth/login";
    }

    // Show forgot password form
    @GetMapping("/forgot-password")
    public String showForgotPasswordForm(Model model) {
        model.addAttribute("forgotPasswordRequest", new ForgotPasswordRequest());
        return "public/authentication/forgot-password";
    }

    // Process forgot password (send OTP)
    @PostMapping("/forgot-password")
    public String processForgotPassword(
            @Valid @ModelAttribute ForgotPasswordRequest request,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) {

        // Validation
        if (bindingResult.hasErrors()) {
            model.addAttribute("error", "Vui lòng nhập email hợp lệ.");
            return "public/authentication/forgot-password";
        }

        String email = request.getEmail().trim().toLowerCase();

        // Validate email format
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            model.addAttribute("error", "Email không đúng định dạng.");
            return "public/authentication/forgot-password";
        }

        // send otp to email
        otpService.sendOtp(email);

        // Redirect to OTP verification page (always, for security)
        redirectAttributes.addFlashAttribute("email", email);
        redirectAttributes.addFlashAttribute("message",
                "Nếu email tồn tại trong hệ thống, mã OTP đã được gửi. Vui lòng kiểm tra email của bạn.");

        return "redirect:/verify-otp";
    }

    // Show OTP verification form
    @GetMapping("/verify-otp")
    public String showVerifyOtpForm(@ModelAttribute("email") String email, Model model) {
        if (email == null || email.isEmpty()) {
            return "redirect:/forgot-password";
        }

        VerifyOtpRequest request = new VerifyOtpRequest();
        request.setEmail(email);
        model.addAttribute("verifyOtpRequest", request);

        // Add cooldown info for resend button
        boolean onCooldown = otpService.isResendOnCooldown(email);
        model.addAttribute("resendOnCooldown", onCooldown);
        if (onCooldown) {
            model.addAttribute("resendCooldownSeconds", otpService.getResendCooldownRemaining(email));
        }

        return "public/authentication/verify-otp";
    }

    // Process OTP verification
    @PostMapping("/verify-otp")
    public String processVerifyOtp(
            @Valid @ModelAttribute VerifyOtpRequest request,
            BindingResult bindingResult,
//            RedirectAttributes redirectAttributes,
            Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("error", "Vui lòng nhập đầy đủ thông tin.");
            model.addAttribute("verifyOtpRequest", request);
            return "public/authentication/verify-otp";
        }
        String email = request.getEmail().trim().toLowerCase();
        String otp = request.getOtp().trim();

        // Validate OTP
        OTPService.OtpValidationResult result = otpService.validateOtp(email, otp);

        if (!result.isValid()) {
            model.addAttribute("error", result.getMessage());
            model.addAttribute("verifyOtpRequest", request);

            // Add cooldown info
            boolean onCooldown = otpService.isResendOnCooldown(email);
            model.addAttribute("resendOnCooldown", onCooldown);
            if (onCooldown) {
                model.addAttribute("resendCooldownSeconds", otpService.getResendCooldownRemaining(email));
            }

            return "public/authentication/verify-otp";
        }

        // OTP is valid - generate reset token
        String resetToken = otpService.generateResetToken(email);

        // Redirect to reset password page with token
        return "redirect:/reset-password?token=" + resetToken;
    }

    // Resend OTP endpoint
    @PostMapping("/resend-otp")
    public String resendOtp(@RequestParam("email") String email,
                            RedirectAttributes redirectAttributes,
                            Model model) {
        System.out.println("Resending otp...");
        email = email.trim().toLowerCase();
        // Check cooldown
        if (otpService.isResendOnCooldown(email)) {
            long remainingSeconds = otpService.getResendCooldownRemaining(email);
            model.addAttribute("error",
                    "Vui lòng đợi " + remainingSeconds + " giây trước khi gửi lại OTP.");
            model.addAttribute("verifyOtpRequest", new VerifyOtpRequest(email, ""));
            model.addAttribute("resendOnCooldown", true);
            model.addAttribute("resendCooldownSeconds", remainingSeconds);
            return "public/authentication/verify-otp";
        }

        // Only resend if email exists and has an active OTP or existed before
        boolean sentOtp = otpService.resendEmail(email);

        if (!sentOtp) {
            model.addAttribute("error",
                    "Đã xảy ra lỗi trong quá trình gửi otp.");
            model.addAttribute("verifyOtpRequest", new VerifyOtpRequest(email, ""));
            return "public/authentication/verify-otp";
        }
        redirectAttributes.addFlashAttribute("email", email);
        redirectAttributes.addFlashAttribute("message", "Mã OTP mới đã được gửi.");

        return "redirect:/verify-otp";
    }

    // Show reset password form
    @GetMapping("/reset-password")
    public String showResetPasswordForm(@RequestParam("token") String token, Model model) {
        // Validate token
        String email = otpService.validateResetToken(token);

        if (email == null) {
            model.addAttribute("error", "Liên kết đã hết hạn hoặc không hợp lệ. Vui lòng thực hiện lại quy trình.");
            return "public/authentication/forgot-password";
        }

        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setToken(token);
        model.addAttribute("resetPasswordRequest", request);

        return "public/authentication/reset-password";
    }

    // Process reset password
    @PostMapping("/reset-password")
    public String processResetPassword(
            @Valid @ModelAttribute ResetPasswordRequest request,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("error", Objects.requireNonNull(bindingResult.getFieldError()).getDefaultMessage());
            model.addAttribute("resetPasswordRequest", request);
            return "public/authentication/reset-password";
        }

        String token = request.getToken();
        String newPassword = request.getNewPassword();
        String confirmPassword = request.getConfirmPassword();

        // Validate token
        String email = otpService.validateResetToken(token);
        if (email == null) {
            model.addAttribute("error", "Liên kết đã hết hạn hoặc không hợp lệ.");
            return "public/authentication/forgot-password";
        }

        // Validate password match
        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("error", "Mật khẩu xác nhận không khớp.");
            model.addAttribute("resetPasswordRequest", request);
            return "public/authentication/reset-password";
        }

        // Validate password strength
        String passwordError = validatePasswordStrength(newPassword);
        if (passwordError != null) {
            model.addAttribute("error", passwordError);
            model.addAttribute("resetPasswordRequest", request);
            return "public/authentication/reset-password";
        }

        // Find customer
        Customer customer = customerService.findCusByEmail(email);
        if (customer == null) {
            // Should not happen if token is valid, but handle gracefully
            model.addAttribute("error", "Không tìm thấy tài khoản.");
            return "public/authentication/forgot-password";
        }


        // Encode and update password
        String encodedPassword = passwordEncoder.encode(newPassword);
        customer.setPasswordHash(encodedPassword);
        customerService.save(customer);

        // Invalidate reset token
        otpService.invalidateResetToken(token);

        // Success - redirect to login with success message
        redirectAttributes.addFlashAttribute("success",
                "Mật khẩu đã được đặt lại thành công. Vui lòng đăng nhập bằng mật khẩu mới.");

        return "redirect:/auth/login";
    }

    private String validatePasswordStrength(String password) {
        if (password.length() < MIN_PASSWORD_LENGTH) {
            return "Mật khẩu phải có ít nhất " + MIN_PASSWORD_LENGTH + " ký tự.";
        }

        boolean hasUppercase = false;
        boolean hasLowercase = false;
        boolean hasDigit = false;
        boolean hasSpecial = false;

        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) hasUppercase = true;
            else if (Character.isLowerCase(c)) hasLowercase = true;
            else if (Character.isDigit(c)) hasDigit = true;
            else hasSpecial = true;
        }

        if (!hasUppercase || !hasLowercase || !hasDigit || !hasSpecial) {
            return "Mật khẩu phải bao gồm chữ hoa, chữ thường, số và ký tự đặc biệt.";
        }
        // Valid
        return null;
    }
}
