package vn.edu.fpt.booknow.controllers;

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
import vn.edu.fpt.booknow.repositories.CustomerRepository;
import vn.edu.fpt.booknow.services.MailService;
import vn.edu.fpt.booknow.services.OTPService;

import java.util.Optional;
import java.util.regex.Pattern;

@Controller
public class ForgotPasswordController {

    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
    private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);
    private static final int MIN_PASSWORD_LENGTH = 8;

    private final CustomerRepository customerRepository;
    private final OTPService otpService;
    private final MailService mailService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public ForgotPasswordController(CustomerRepository customerRepository,
                                    OTPService otpService,
                                    MailService mailService,
                                    PasswordEncoder passwordEncoder) {
        this.customerRepository = customerRepository;
        this.otpService = otpService;
        this.mailService = mailService;
        this.passwordEncoder = passwordEncoder;
    }

    // Step 1: Show forgot password form
    @GetMapping("/forgot-password")
    public String showForgotPasswordForm(Model model) {
        model.addAttribute("forgotPasswordRequest", new ForgotPasswordRequest());
        return "public/authentication/forgot-password";
    }

    // Step 1: Process forgot password (send OTP)
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

        // Check if resend is on cooldown
        if (otpService.isResendOnCooldown(email)) {
            long remainingSeconds = otpService.getResendCooldownRemaining(email);
            model.addAttribute("error", 
                "Vui lòng đợi " + remainingSeconds + " giây trước khi yêu cầu OTP mới.");
            model.addAttribute("email", email);
            return "public/authentication/forgot-password";
        }

        // Check if email exists (but don't reveal this to user for security)
        Optional<Customer> customerOpt = customerRepository.findCustomerByEmail(email);

        // Always show success message even if email doesn't exist (security best practice)
        if (customerOpt.isPresent()) {
            // Generate and save OTP
            String otp = otpService.generateAndSaveOtp(email);

            // Send OTP via email
            try {
                mailService.sendOtp(email, otp);
                System.out.println("sent otp to" + otp);
            } catch (Exception e) {
                // Log error but still show generic message
                System.err.println("Failed to send OTP email: " + e.getMessage());
            }

            // Set resend cooldown
            otpService.setResendCooldown(email);
        }

        // Redirect to OTP verification page (always, for security)
        redirectAttributes.addFlashAttribute("email", email);
        redirectAttributes.addFlashAttribute("message", 
            "Nếu email tồn tại trong hệ thống, mã OTP đã được gửi. Vui lòng kiểm tra email của bạn.");

        return "redirect:/verify-otp";
    }

    // Step 2: Show OTP verification form
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

    // Step 2: Process OTP verification
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
        Optional<Customer> customerOpt = customerRepository.findCustomerByEmail(email);
        if (customerOpt.isPresent()) {
            // Generate new OTP
            String otp = otpService.generateAndSaveOtp(email);

            // Send OTP
            try {
                System.out.println("sent otp to" + otp);
                mailService.sendOtp(email, otp);
            } catch (Exception e) {
                System.err.println("Failed to resend OTP: " + e.getMessage());
            }

            // Set cooldown
            otpService.setResendCooldown(email);
        }

        redirectAttributes.addFlashAttribute("email", email);
        redirectAttributes.addFlashAttribute("message", "Mã OTP mới đã được gửi.");
        
        return "redirect:/verify-otp";
    }

    // Step 3: Show reset password form
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

    // Step 3: Process reset password
    @PostMapping("/reset-password")
    public String processResetPassword(
            @Valid @ModelAttribute ResetPasswordRequest request,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("error", "Vui lòng nhập đầy đủ thông tin.");
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
        Optional<Customer> customerOpt = customerRepository.findCustomerByEmail(email);
        if (customerOpt.isEmpty()) {
            // Should not happen if token is valid, but handle gracefully
            model.addAttribute("error", "Không tìm thấy tài khoản.");
            return "public/authentication/forgot-password";
        }

        Customer customer = customerOpt.get();

        // Encode and update password
        String encodedPassword = passwordEncoder.encode(newPassword);
        customer.setPasswordHash(encodedPassword);
        customerRepository.save(customer);

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

        if (!hasUppercase || !hasLowercase || !hasDigit) {
            return "Mật khẩu phải chứa ít nhất 1 chữ hoa, 1 chữ thường và 1 số.";
        }

        return null; // Valid
    }
}
