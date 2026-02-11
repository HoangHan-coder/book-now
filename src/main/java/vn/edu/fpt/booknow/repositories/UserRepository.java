package vn.edu.fpt.booknow.repositories;




import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.edu.fpt.booknow.entities.StaffAccount;

import java.util.List;
public interface UserRepository extends JpaRepository<StaffAccount, Long> {

    @Query("""
        SELECT s FROM StaffAccount s
        WHERE (:role IS NULL OR s.role = :role)
          AND (:status IS NULL OR s.status = :status)
    """)
    List<StaffAccount> findByRoleAndStatus(@Param("role") String role, @Param("status") String status);
}

