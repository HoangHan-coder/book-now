package vn.edu.fpt.booknow.services.customer;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import vn.edu.fpt.booknow.entities.Customer;
import vn.edu.fpt.booknow.repositories.CustomerRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class ChangePasswordService {

    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;

    public ChangePasswordService(
            CustomerRepository customerRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Map<String, String> changePassword(
            Long customerId,
            String currentPassword,
            String newPassword,
            String confirmPassword
    ) {
        Map<String, String> errors = new HashMap<>();


        if (currentPassword == null || currentPassword.isBlank()) {
            errors.put("currentPassword", "Vui lòng nhập mật khẩu hiện tại");
        }

        if (newPassword == null || newPassword.isBlank()) {
            errors.put("newPassword", "Vui lòng nhập mật khẩu mới");
        }

        if (confirmPassword == null || confirmPassword.isBlank()) {
            errors.put("confirmPassword", "Vui lòng xác nhận mật khẩu mới");
        }

        if (!errors.isEmpty()) return errors;


        Optional<Customer> optional = customerRepository.findById(customerId);
        if (optional.isEmpty()) {
            errors.put("global", "Không tìm thấy tài khoản");
            return errors;
        }

        Customer customer = optional.get();


        if (!passwordEncoder.matches(currentPassword, customer.getPasswordHash())) {
            errors.put("currentPassword", "Mật khẩu hiện tại không đúng");
            return errors;
        }


        if (newPassword.length() < 8
                || !newPassword.matches(".*[A-Z].*")
                || !newPassword.matches(".*[a-z].*")
                || !newPassword.matches(".*\\d.*")) {
            errors.put(
                    "newPassword",
                    "Mật khẩu phải ≥ 8 ký tự, gồm chữ hoa, chữ thường và số"
            );
            return errors;
        }

        if (!newPassword.equals(confirmPassword)) {
            errors.put("confirmPassword", "Mật khẩu xác nhận không khớp");
            return errors;
        }


        if (passwordEncoder.matches(newPassword, customer.getPasswordHash())) {
            errors.put("newPassword", "Mật khẩu mới không được trùng mật khẩu cũ");
            return errors;
        }

        // ===== Hash & save =====
        customer.setPasswordHash(passwordEncoder.encode(newPassword));
        customerRepository.save(customer);

        return errors; // empty = success
    }
}
