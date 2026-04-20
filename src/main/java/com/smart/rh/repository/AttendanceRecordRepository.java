package com.smart.rh.repository;

import com.smart.rh.entity.AttendanceRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, Long> {
    List<AttendanceRecord> findByEmployeeIdOrderByEventTimestampDesc(Long employeeId);

    Page<AttendanceRecord> findByEmployeeIdOrderByEventTimestampDesc(Long employeeId, Pageable pageable);

    @Query("SELECT a FROM AttendanceRecord a WHERE a.employee.id = :employeeId AND a.eventTimestamp BETWEEN :start AND :end ORDER BY a.eventTimestamp DESC")
    List<AttendanceRecord> findByEmployeeAndDateRange(@Param("employeeId") Long employeeId,
            @Param("start") Instant start, @Param("end") Instant end);

    @Query("SELECT a FROM AttendanceRecord a WHERE a.employee.id = :employeeId AND a.eventTimestamp > :since ORDER BY a.eventTimestamp DESC")
    List<AttendanceRecord> findRecentByEmployee(@Param("employeeId") Long employeeId, @Param("since") Instant since);
}
