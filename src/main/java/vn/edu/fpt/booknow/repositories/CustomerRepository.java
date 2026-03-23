package vn.edu.fpt.booknow.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.edu.fpt.booknow.model.entities.Customer;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByEmail(String email);
    Customer getCustomerByEmail(String email);
    Optional<Customer> findCustomerByEmail(String email);

    @Query("""
    SELECT c FROM Customer c
    WHERE (:status IS NULL OR c.status = :status)
    AND (:keyword IS NULL OR LOWER(c.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')))
    AND c.isDeleted = false
    ORDER BY c.createdAt DESC
    """)
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
