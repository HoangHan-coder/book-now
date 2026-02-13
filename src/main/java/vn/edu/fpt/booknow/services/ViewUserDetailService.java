package vn.edu.fpt.booknow.services;


import org.springframework.stereotype.Service;
import vn.edu.fpt.booknow.dto.UserDetailDTO;
import vn.edu.fpt.booknow.entities.Customer;
import vn.edu.fpt.booknow.entities.StaffAccount;
import vn.edu.fpt.booknow.repositories.CustomerRepository;
import vn.edu.fpt.booknow.repositories.StaffAccountRepository;

@Service
public class ViewUserDetailService {

    private final StaffAccountRepository staffRepo;
    private final CustomerRepository customerRepo;

    public ViewUserDetailService(StaffAccountRepository staffRepo,
                                 CustomerRepository customerRepo) {
        this.staffRepo = staffRepo;
        this.customerRepo = customerRepo;
    }

    // UC-17.2: View User Detail
    public UserDetailDTO getUserDetail(String userId, String role) {

        if ("STAFF".equalsIgnoreCase(role) || "ADMIN".equalsIgnoreCase(role)) {

            StaffAccount staff = staffRepo.findById(Long.parseLong(userId))
                    .orElseThrow(() ->
                            new RuntimeException("User not found"));



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

        } else if ("CUSTOMER".equalsIgnoreCase(role)) {

            Customer customer = customerRepo.findById(Long.parseLong(userId))
                    .orElseThrow(() ->
                            new RuntimeException("User not found"));


            return new UserDetailDTO(
                    String.valueOf(customer.getCustomerId()),
                    customer.getFullName(),
                    customer.getEmail(),
                    customer.getPhone(),
                    "CUSTOMER",
                    customer.getAvatarUrl(),
                    customer.getStatus(),
                    customer.getCreatedAt()
            );

        } else {
            throw new RuntimeException("Invalid role");
        }
    }
}
