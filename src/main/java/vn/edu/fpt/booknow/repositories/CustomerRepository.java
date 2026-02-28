package vn.edu.fpt.booknow.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.fpt.booknow.entities.Customer;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Customer getCustomerByEmail(String email);
}
