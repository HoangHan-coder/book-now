package vn.edu.fpt.booknow.services.admin;

import org.springframework.stereotype.Service;
import vn.edu.fpt.booknow.model.dto.UserDetailDTO;
import vn.edu.fpt.booknow.model.entities.StaffAccount;
import vn.edu.fpt.booknow.repositories.StaffAccountRepository;

import java.util.Optional;
import java.util.regex.Pattern;

// Control Class (COMET)
// UC-17.x: Edit Staff Account
@Service
public class EditStaffAccountService {

    private final StaffAccountRepository repository;

    public EditStaffAccountService(StaffAccountRepository repository) {
        this.repository = repository;
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
                staff.getCreatedAt()
        );
    }

    // UC-17.x: Update Staff Account
    public void updateStaffAccount(Long id,
                                   String fullName,
                                   String phone,
                                   String role,
                                   String status) {

        StaffAccount staff = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Staff not found"));

        validateInput(fullName, phone, role, status);

        staff.setFullName(fullName);
        staff.setPhone(phone);
        staff.setRole(role);
        staff.setStatus(status);

        repository.save(staff);
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

        if (!role.equals("STAFF") && !role.equals("ADMIN")) {
            throw new RuntimeException("Invalid role");
        }

        if (!status.equals("ACTIVE") && !status.equals("INACTIVE")) {
            throw new RuntimeException("Invalid status");
        }
    }
}