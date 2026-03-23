package vn.edu.fpt.booknow.repositories;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.edu.fpt.booknow.model.entities.StaffAccount;

import java.util.List;
import java.util.Optional;
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

    // UC-17.3: Update staff account status
    @Transactional
    @Modifying
    @Query("UPDATE StaffAccount s SET s.status = :status WHERE s.staffAccountId = :staffAccountId")
    int updateStatus(@Param("staffAccountId") Long staffAccountId,
                     @Param("status") String status);


    Optional<StaffAccount> findStaffAccountByEmail(String email);
}

