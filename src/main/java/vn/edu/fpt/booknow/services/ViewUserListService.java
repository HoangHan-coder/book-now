package vn.edu.fpt.booknow.services;

import vn.edu.fpt.booknow.entities.Customer;
import vn.edu.fpt.booknow.entities.StaffAccount;
import vn.edu.fpt.booknow.repositories.CustomerRepository;
import vn.edu.fpt.booknow.repositories.UserRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ViewUserListService {

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;

    public ViewUserListService(UserRepository userRepository,
                               CustomerRepository customerRepository) {
        this.userRepository = userRepository;
        this.customerRepository = customerRepository;
    }


    public List<Object> getUserList(String roleFilter, String statusFilter) {
        List<Object> result = new ArrayList<>();

        //lấy STAFF/ADMIN
        if (roleFilter == null || (!roleFilter.equals("CUSTOMER"))) {
            result.addAll(userRepository.findByRoleAndStatus(roleFilter, statusFilter));
        }

        //lấy CUSTOMER
        if (roleFilter == null || roleFilter.equals("CUSTOMER")) {
            result.addAll(customerRepository.findByStatus(statusFilter));
        }

        return result;
    }
}