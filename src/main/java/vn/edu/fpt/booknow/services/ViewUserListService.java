package vn.edu.fpt.booknow.services;

import vn.edu.fpt.booknow.dto.UserDTO;
import vn.edu.fpt.booknow.entities.Customer;
import vn.edu.fpt.booknow.entities.StaffAccount;
import vn.edu.fpt.booknow.repositories.CustomerRepository;
import vn.edu.fpt.booknow.repositories.StaffAccountRepository;
import org.springframework.stereotype.Service;

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

    public List<UserDTO> getUserList(String roleFilter, String statusFilter) {

        List<UserDTO> result = new ArrayList<>();

        // STAFF + ADMIN
        if (roleFilter == null || !roleFilter.equals("CUSTOMER")) {

            List<StaffAccount> staffList =
                    staffAccountRepository.findByRoleAndStatus(roleFilter, statusFilter);

            for (StaffAccount staff : staffList) {
                result.add(new UserDTO(
                        staff.getStaffAccountId(),
                        staff.getFullName(),
                        staff.getEmail(),
                        staff.getRole(),
                        staff.getStatus()
                ));
            }
        }

        // CUSTOMER
        if (roleFilter == null || roleFilter.equals("CUSTOMER")) {

            List<Customer> customerList =
                    customerRepository.findByStatus(statusFilter);

            for (Customer customer : customerList) {
                result.add(new UserDTO(
                        customer.getCustomerId(),
                        customer.getFullName(),
                        customer.getEmail(),
                        "CUSTOMER", // vì bảng customer không có role
                        customer.getStatus()
                ));
            }
        }

        return result;
    }
}
