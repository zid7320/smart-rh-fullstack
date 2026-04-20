package com.smart.rh.repository;

import com.smart.rh.entity.AttendanceVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceVerificationRepository extends JpaRepository<AttendanceVerification, Long> {
    Optional<AttendanceVerification> findByAttendanceRecordId(Long attendanceRecordId);

    List<AttendanceVerification> findByVerificationStatus(String status);
}
