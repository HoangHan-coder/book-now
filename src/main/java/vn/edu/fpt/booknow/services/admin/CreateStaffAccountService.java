package vn.edu.fpt.booknow.services.admin;

import vn.edu.fpt.booknow.model.dto.StaffAccountCreateDTO;
import vn.edu.fpt.booknow.model.entities.StaffAccount;
import vn.edu.fpt.booknow.repositories.StaffAccountRepository;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
public class CreateStaffAccountService {

    private final StaffAccountRepository repository;

    public CreateStaffAccountService(StaffAccountRepository repository) {
        this.repository = repository;
    }

    // UC-17.X: Create Staff Account
    public void createStaffAccount(StaffAccountCreateDTO dto) {

        validateInput(dto);

        Optional<StaffAccount> existing =
                repository.findStaffAccountByEmail(dto.getEmail());

        if (existing.isPresent()) {
            throw new RuntimeException("Email đã tồn tại");
        }

        StaffAccount account = buildEntity(dto);

        repository.save(account);
    }

    private void validateInput(StaffAccountCreateDTO dto) {

        if (dto.getFullName() == null || dto.getFullName().isBlank()) {
            throw new RuntimeException("Tên không được bỏ trống");
        }

        if (dto.getFullName().matches(".*\\d.*")) {
            throw new RuntimeException("Tên không được chứa số");
        }

        if (dto.getPhone() == null ||
                !Pattern.matches("^\\d{10}$", dto.getPhone())) {

            throw new RuntimeException("Số điện thoại phải đủ 10 số");
        }

        if (dto.getEmail() == null ||
                !dto.getEmail().contains("@")) {

            throw new RuntimeException("Email không hợp lệ");
        }

        if (dto.getPassword() == null || dto.getPassword().isBlank()) {
            throw new RuntimeException("Mật khẩu không được bỏ trống");
        }

        if (!isValidPassword(dto.getPassword())) {
            throw new RuntimeException(
                    "Mật khẩu phải có ít nhất 8 ký tự, gồm chữ hoa, chữ thường và số"
            );
        }

        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            throw new RuntimeException("Mật khẩu xác nhận không khớp");
        }
    }

    private StaffAccount buildEntity(StaffAccountCreateDTO dto) {

        StaffAccount account = new StaffAccount();

        account.setFullName(dto.getFullName());
        account.setPhone(dto.getPhone());
        account.setEmail(dto.getEmail());

        account.setPasswordHash(hashPassword(dto.getPassword()));

        account.setRole(dto.getRole());

        account.setStatus("ACTIVE");

        account.setCreatedAt(LocalDateTime.now());

        account.setIsDeleted(false);

        return account;
    }

    private String hashPassword(String password) {

        return org.springframework.security.crypto.bcrypt.BCrypt
                .hashpw(password,
                        org.springframework.security.crypto.bcrypt.BCrypt.gensalt());
    }

    private boolean isValidPassword(String password){

        String regex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$";

        return password.matches(regex);
    }

}