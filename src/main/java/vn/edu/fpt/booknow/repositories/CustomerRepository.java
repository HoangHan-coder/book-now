package vn.edu.fpt.booknow.repositories;

import org.springframework.data.jpa.repository.Modifying;
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
    // UC-17.3: Update customer account status
    @Modifying
    @Query("UPDATE Customer c SET c.status = :status WHERE c.customerId = :customerId")
    int updateStatus(@Param("customerId") Integer customerId,
                     @Param("status") String status);
}
