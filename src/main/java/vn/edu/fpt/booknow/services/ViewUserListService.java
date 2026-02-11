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

    public List<UserDTO> getUserList(String roleFilter,
                                     String statusFilter,
                                     String keyword) {

        List<UserDTO> result = new ArrayList<>();

        // STAFF (trừ khi filter là CUSTOMER)
        if (roleFilter == null || !roleFilter.equals("CUSTOMER")) {

            List<StaffAccount> staffList =
                    staffAccountRepository.searchStaff(
                            roleFilter,
                            statusFilter,
                            keyword
                    );

            staffList.forEach(s ->
                    result.add(new UserDTO(
                            s.getStaffAccountId(),
                            s.getFullName(),
                            s.getEmail(),
                            s.getRole(),
                            s.getStatus()
                    ))
            );
        }

        // CUSTOMER
        if (roleFilter == null || roleFilter.equals("CUSTOMER")) {

            List<Customer> customerList =
                    customerRepository.searchCustomer(
                            statusFilter,
                            keyword
                    );

            customerList.forEach(c ->
                    result.add(new UserDTO(
                            c.getCustomerId(),
                            c.getFullName(),
                            c.getEmail(),
                            "CUSTOMER",
                            c.getStatus()
                    ))
            );
        }

        return result;
    }
}
