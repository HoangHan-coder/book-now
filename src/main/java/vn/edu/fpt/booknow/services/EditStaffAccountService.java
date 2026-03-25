package vn.edu.fpt.booknow.services;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import vn.edu.fpt.booknow.model.dto.UserDetailDTO;
import vn.edu.fpt.booknow.model.entities.StaffAccount;
import vn.edu.fpt.booknow.repositories.StaffAccountRepository;
import vn.edu.fpt.booknow.services.MailService;

import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

// Control Class (COMET)
// UC-17.x: Edit Staff Account
@Service
public class EditStaffAccountService {

    private final StaffAccountRepository repository;
    private final Cloudinary cloudinary;
    private final MailService mailService;
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    public EditStaffAccountService(StaffAccountRepository repository,
            Cloudinary cloudinary,
            MailService mailService) {
        this.repository = repository;
        this.cloudinary = cloudinary;
        this.mailService = mailService;
    }

    // UC-17.x: Get Staff Account for Edit
    public UserDetailDTO getStaffAccountById(Long id) {

        Optional<StaffAccount> optional = repository.findById(id);

        if (optional.isEmpty()) {
            throw new RuntimeException("Staff account not found");
        }

        StaffAccount staff = optional.get();

        return new UserDetailDTO(
                String.valueOf(staff.getStaffAccountId()),
                staff.getFullName(),
                staff.getEmail(),
                staff.getPhone(),
                staff.getRole(),
                staff.getAvatarUrl(),
                staff.getStatus(),
                staff.getCreatedAt());
    }

    private void uploadAvatar(MultipartFile avatar, StaffAccount account) {

        try {

            if (avatar != null && !avatar.isEmpty()) {

                if (avatar.getSize() > MAX_FILE_SIZE) {
                    throw new RuntimeException("Avatar tối đa 10MB");
                }

                if (account.getAvatarPublicId() != null) {
                    cloudinary.uploader().destroy(account.getAvatarPublicId(), ObjectUtils.emptyMap());
                }

                Map uploadResult = cloudinary.uploader().upload(
                        avatar.getBytes(),
                        ObjectUtils.asMap(
                                "folder", "booknow/avatar",
                                "transformation", "c_fill,w_300,h_300"));

                account.setAvatarUrl(uploadResult.get("secure_url").toString());
                account.setAvatarPublicId(uploadResult.get("public_id").toString());
            }

        } catch (Exception e) {
            throw new RuntimeException("Upload avatar thất bại");
        }
    }

    // UC-17.x: Update Staff Account
    public void updateStaffAccount(Long id,
            String fullName,
            String phone,
            String role,
            String status,
            String newPassword,
            String confirmNewPassword,
            MultipartFile avatar) {

        StaffAccount staff = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Staff not found"));

        validateInput(fullName, phone, role, status);

        staff.setFullName(fullName);
        staff.setPhone(phone);
        staff.setRole(role);
        staff.setStatus(status);

        // UPDATE PASSWORD
        boolean isPasswordChanged = updatePassword(staff, newPassword, confirmNewPassword);

        // upload avatar nếu có
        uploadAvatar(avatar, staff);

        repository.save(staff);
        // gửi mail
        if (isPasswordChanged) {
            mailService.sendNewPassword(staff.getEmail(), newPassword);
        }

    }

    // UC-17.x: Validate input rules
    private void validateInput(String fullName,
            String phone,
            String role,
            String status) {

        if (fullName == null || fullName.isBlank()) {
            throw new RuntimeException("Full name is required");
        }

        Pattern namePattern = Pattern.compile("^[A-Za-zÀ-ỹ\\s]+$");

        if (!namePattern.matcher(fullName).matches()) {
            throw new RuntimeException("Name must not contain numbers");
        }

        Pattern phonePattern = Pattern.compile("^\\d{10}$");

        if (!phonePattern.matcher(phone).matches()) {
            throw new RuntimeException("Phone must contain exactly 10 digits");
        }

        if (!role.equals("STAFF") && !role.equals("ADMIN") && !role.equals("HOUSEKEEPING")) {
            throw new RuntimeException("Invalid role");
        }

        if (!status.equals("ACTIVE") && !status.equals("INACTIVE")) {
            throw new RuntimeException("Invalid status");
        }
    }

    private boolean updatePassword(StaffAccount staff,
            String newPassword,
            String confirmPassword) {

        if (newPassword == null || newPassword.isBlank()) {
            return false;
        }

        validatePasswordRule(newPassword);

        if (!newPassword.equals(confirmPassword)) {
            throw new RuntimeException("Confirm password không khớp");
        }

        String hashedPassword = hashPassword(newPassword);
        staff.setPasswordHash(hashedPassword);

        return true;
    }

    private void validatePasswordRule(String password) {

        if (password.length() < 8) {
            throw new RuntimeException("Password phải ít nhất 8 ký tự");
        }

        Pattern pattern = Pattern.compile(
                "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d).+$");

        if (!pattern.matcher(password).matches()) {
            throw new RuntimeException(
                    "Password phải có chữ hoa, chữ thường và số");
        }
    }

    private String hashPassword(String password) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);
        return encoder.encode(password);
    }
}