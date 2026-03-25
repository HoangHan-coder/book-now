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

        List<StaffAccount> staffList =
                staffAccountRepository.searchStaff(roleFilter, statusFilter, null);
        List<Customer> customerList = new ArrayList<>();

        if (roleFilter == null || roleFilter.equals("CUSTOMER")) {
            customerList = customerRepository.searchCustomer(statusFilter, null);
        }

        // STAFF
        for (StaffAccount s : staffList) {

            boolean matchKeyword = true;

            if (keywordNormalized != null) {
                String nameNormalized = TextUtils.removeAccent(s.getFullName());
                matchKeyword = nameNormalized.contains(keywordNormalized);
            }

            if (matchKeyword) {
                result.add(new UserDTO(
                        s.getStaffAccountId(),
                        s.getFullName(),
                        s.getEmail(),
                        s.getRole(),
                        s.getStatus()
                ));
            }
        }

        // CUSTOMER
        for (Customer c : customerList) {

            boolean matchKeyword = true;

            if (keywordNormalized != null) {
                String nameNormalized = TextUtils.removeAccent(c.getFullName());
                matchKeyword = nameNormalized.contains(keywordNormalized);
            }

            if (matchKeyword) {
                result.add(new UserDTO(
                        c.getCustomerId(),
                        c.getFullName(),
                        c.getEmail(),
                        "CUSTOMER",
                        c.getStatus()
                ));
            }
        }

        return result;
    }
}