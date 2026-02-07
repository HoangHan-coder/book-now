package vn.edu.fpt.booknow.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.edu.fpt.booknow.entities.Booking;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    @Query(value = """
        SELECT 
            ((DATEPART(WEEKDAY, check_in_time) + @@DATEFIRST - 2) % 7) + 1 AS dayIndex,
            SUM(total_amount) AS revenue
        FROM Booking
        WHERE
            booking_status = 'PAID'
            AND check_in_time BETWEEN :startDate AND :endDate
        GROUP BY ((DATEPART(WEEKDAY, check_in_time) + @@DATEFIRST - 2) % 7) + 1
        ORDER BY dayIndex
        """, nativeQuery = true)
    List<Object[]> getWeeklyRevenue(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}
