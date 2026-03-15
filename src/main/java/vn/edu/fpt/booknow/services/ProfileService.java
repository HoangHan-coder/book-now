package vn.edu.fpt.booknow.services;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import vn.edu.fpt.booknow.model.entities.Customer;
import vn.edu.fpt.booknow.repositories.CustomerRepository;

@Service
public class ProfileService {
    @Autowired
    private  CustomerRepository customerRepository;

    public Customer profileDetailByEmail(String email) {

        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email must not be null or blank");
        }

        try {
            Customer customer = customerRepository.findByEmail(email)
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Customer not found with email = " + email));

            if (Boolean.TRUE.equals(customer.getIsDeleted())) {
                throw new IllegalStateException("Customer has been deleted");
            }

            if (!"ACTIVE".equalsIgnoreCase(customer.getStatus())) {
                throw new IllegalStateException("Customer is inactive");
            }

            return customer;

        } catch (DataAccessException ex) {
            throw new RuntimeException("Database error while fetching customer", ex);
        }
    }


}
