package vn.edu.fpt.booknow.services;

import vn.edu.fpt.booknow.model.dto.UserDTO;
import vn.edu.fpt.booknow.model.entities.Customer;
import vn.edu.fpt.booknow.model.entities.StaffAccount;
import vn.edu.fpt.booknow.repositories.CustomerRepository;
import vn.edu.fpt.booknow.repositories.StaffAccountRepository;
import org.springframework.stereotype.Service;
import vn.edu.fpt.booknow.utils.TextUtils;

import java.util.ArrayList;
import java.util.List;

@Service
public class ViewUserListService {

    private final StaffAccountRepository staffAccountRepository;
    private final CustomerRepository customerRepository;

    public ViewUserListService(StaffAccountRepository staffAccountRepository,
            CustomerRepository customerRepository) {
        this.staffAccountRepository = staffAccountRepository;
        this.customerRepository = customerRepository;
    }

    public List<UserDTO> getUserList(String roleFilter,
            String statusFilter,
            String keyword) {

        List<UserDTO> result = new ArrayList<>();

        String keywordNormalized = null;

        if (keyword != null && !keyword.isBlank()) {
            keywordNormalized = TextUtils.removeAccent(keyword.trim());
        }

        // STAFF
        List<StaffAccount> staffList = staffAccountRepository.searchStaff(roleFilter, statusFilter, null); // ❌ bỏ
                                                                                                           // keyword DB

        for (StaffAccount s : staffList) {

            String nameNormalized = TextUtils.removeAccent(s.getFullName());

            if (keywordNormalized == null || nameNormalized.contains(keywordNormalized)) {
                result.add(new UserDTO(
                        s.getStaffAccountId(),
                        s.getFullName(),
                        s.getEmail(),
                        s.getRole(),
                        s.getStatus()));
            }
        }

        // CUSTOMER
        List<Customer> customerList = customerRepository.searchCustomer(statusFilter, null);

        for (Customer c : customerList) {

            String nameNormalized = TextUtils.removeAccent(c.getFullName());

            if (keywordNormalized == null || nameNormalized.contains(keywordNormalized)) {
                result.add(new UserDTO(
                        c.getCustomerId(),
                        c.getFullName(),
                        c.getEmail(),
                        "CUSTOMER",
                        c.getStatus()));
            }
        }

        return result;
    }
}
