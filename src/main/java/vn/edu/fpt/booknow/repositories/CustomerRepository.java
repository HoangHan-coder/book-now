package vn.edu.fpt.booknow.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.edu.fpt.booknow.entities.Customer;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

}
