package vn.edu.fpt.booknow.services.customer;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import vn.edu.fpt.booknow.entities.Customer;
import vn.edu.fpt.booknow.repositories.CustomerRepository;

@Service
public class ProfileService {
    @Autowired
    private  CustomerRepository customerRepository;

    public Customer profileDetailById(Long id) {

        if (id == null) {
            throw new IllegalArgumentException("Customer id must not be null");
        }

        try {
            Customer customer = customerRepository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Customer not found with id = " + id));

            if (Boolean.TRUE.equals(customer.getIsDeleted())) {
                throw new IllegalStateException("Customer has been deleted");
            }

            if (!"ACTIVE".equals(customer.getStatus())) {
                throw new IllegalStateException("Customer is inactive");
            }

            return customer;

        } catch (DataAccessException ex) {
            throw new RuntimeException("Database error while fetching customer", ex);
        }
    }


}
