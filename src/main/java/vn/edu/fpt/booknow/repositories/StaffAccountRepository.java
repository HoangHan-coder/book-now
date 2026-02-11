package vn.edu.fpt.booknow.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.edu.fpt.booknow.entities.StaffAccount;

import java.util.List;
public interface StaffAccountRepository extends JpaRepository<StaffAccount, Long> {

    @Query("SELECT s FROM StaffAccount s " +
            "WHERE (:role IS NULL OR s.role = :role) " +
            "AND (:status IS NULL OR s.status = :status) " +
            "AND (:keyword IS NULL OR LOWER(s.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<StaffAccount> searchStaff(
            @Param("role") String role,
            @Param("status") String status,
            @Param("keyword") String keyword
    );


}

