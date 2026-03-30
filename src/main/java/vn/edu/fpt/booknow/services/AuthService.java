package vn.edu.fpt.booknow.services;


import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;
import vn.edu.fpt.booknow.model.entities.Customer;
import vn.edu.fpt.booknow.repositories.CustomerRepository;

import java.time.LocalDateTime;

@Service
public class AuthService {

    @Autowired
    private CustomerRepository customerRepository;


    public Customer findCustomerByEmail(String email) {
        return customerRepository.findCustomerByEmail(email).orElse(null);
    }

    @Transactional
    public Customer Save(String email, String name, String avatar, String publicId) {
        Customer customer = new Customer();
        LocalDateTime now = LocalDateTime.now();
        customer.setEmail(email);
        customer.setFullName(name);
        customer.setAvatarUrl(avatar);
        customer.setAvatarPublicId(publicId);
        customer.setCreatedAt(now);
        customer.setStatus("ACTIVE");
        return customerRepository.save(customer);
    }

    @Transactional
    public Customer Register(String email, String name, String password, String phoneNumber) {
        Customer customer = new Customer();
        customer.setEmail(email);
        customer.setFullName(name);
        customer.setPasswordHash(hashPassword(password));
        customer.setCreatedAt(LocalDateTime.now());
        customer.setStatus("ACTIVE");
        customer.setPhone(phoneNumber);
        if (customerRepository.findCustomerByEmail(email).isPresent()) {
            return null;
        }
        return customerRepository.save(customer);
    }

    public String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt(12));
    }
}
