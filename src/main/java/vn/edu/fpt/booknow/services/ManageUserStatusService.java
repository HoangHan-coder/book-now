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

    @Transactional
    public void changeUserStatus(Long userId, String userType, String status) {

        if (userId == null || userType == null || status == null) {
            throw new IllegalArgumentException("Invalid input data");
        }

        if (!status.equals("ACTIVE") && !status.equals("INACTIVE")) {
            throw new IllegalArgumentException("Invalid status value");
        }

        if (!userType.equalsIgnoreCase("CUSTOMER")) {
            staffAccountRepository.updateStatus(userId, status);
        } else {
            customerRepository.updateStatus(userId.intValue(), status);
        }
    }
}
