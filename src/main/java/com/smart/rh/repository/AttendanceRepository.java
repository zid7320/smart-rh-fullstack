package com.smart.rh.repository;

import com.smart.rh.entity.Attendance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    /** All records newest-first — used by the global history endpoint. */
    Page<Attendance> findAllByOrderByClockedAtDesc(Pageable pageable);

    /** Records for a single employee newest-first — used by the by-employee endpoint. */
    Page<Attendance> findByEmployeIdOrderByClockedAtDesc(Long employeId, Pageable pageable);
}
