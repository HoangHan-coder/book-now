package vn.edu.fpt.booknow.services.customer;


import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vn.edu.fpt.booknow.entities.Customer;
import vn.edu.fpt.booknow.repositories.CustomerRepository;

import java.time.LocalDateTime;
import java.util.Date;

@Service
public class AuthService {

    @Autowired
    private CustomerRepository customerRepository;


    public Customer findCustomerByEmail(String email) {
        return customerRepository.findCustomerByEmail(email).orElse(null);
    }

    @Transactional
    public Customer Save(String email, String name, String avatar) {
        Customer customer = new Customer();
        LocalDateTime now = LocalDateTime.now();
        customer.setEmail(email);
        customer.setFullName(name);
        customer.setAvatarUrl(avatar);
        customer.setCreatedAt(now);
        customer.setStatus("ACTIVE");
        return customerRepository.save(customer);
    }

    @Transactional
    public Customer Register(String email, String name, String password, String phoneNumber) {
        Customer customer = new Customer();
        return customerRepository.save(customer);
    }
}
