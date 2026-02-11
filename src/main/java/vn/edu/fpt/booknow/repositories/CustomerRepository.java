package vn.edu.fpt.booknow.repositories;

import org.springframework.data.repository.query.Param;
import vn.edu.fpt.booknow.entities.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    @Query("SELECT c FROM Customer c " +
            "WHERE (:status IS NULL OR c.status = :status) " +
            "AND (:keyword IS NULL OR LOWER(c.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Customer> searchCustomer(
            @Param("status") String status,
            @Param("keyword") String keyword
    );



}
