package vn.edu.fpt.booknow.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.fpt.booknow.repositories.StaffAccountRepository;
import vn.edu.fpt.booknow.repositories.CustomerRepository;

@Service
public class ManageUserStatusService {

    private final StaffAccountRepository staffAccountRepository;
    private final CustomerRepository customerRepository;

    public ManageUserStatusService(StaffAccountRepository staffAccountRepository,
                                   CustomerRepository customerRepository) {
        this.staffAccountRepository = staffAccountRepository;
        this.customerRepository = customerRepository;
    }

    // UC-17.3: Inactivate / Reactivate User Account
    @Transactional
    public void changeUserStatus(Long userId, String userType, String status) {

        // Validate input
        if (userId == null || userType == null || status == null) {
            throw new IllegalArgumentException("Invalid input data");
        }

        if (!status.equals("ACTIVE") && !status.equals("INACTIVE")) {
            throw new IllegalArgumentException("Invalid status value");
        }

        // Staff account
        if (userType.equalsIgnoreCase("STAFF")
                || userType.equalsIgnoreCase("ADMIN")) {

            staffAccountRepository.updateStatus(userId, status);
        }
        // Customer account
        else if (userType.equalsIgnoreCase("CUSTOMER")) {
            customerRepository.updateStatus(userId.intValue(), status);
        }
        else {
            throw new IllegalArgumentException("Invalid user type");
        }
    }
}
