package com.meta12.SS8911.repository;

import com.meta12.SS8911.entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    @Query("SELECT a FROM Attendance a WHERE a.siteUser.username = :username " +
            "AND YEAR(a.date) = :year " +
            "AND MONTH(a.date) = :month")
    List<Attendance> findByUsernameAndYearAndMonth(@Param("username") String username,
                                                   @Param("year") int year,
                                                   @Param("month") int month);
}