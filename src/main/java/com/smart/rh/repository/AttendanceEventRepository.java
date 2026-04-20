package com.smart.rh.repository;

import com.smart.rh.entity.AttendanceEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;

public interface AttendanceEventRepository extends JpaRepository<AttendanceEvent, Long> {

       /**
        * Get recent attendance events for dashboard (last N events)
        */
       @Query("SELECT ae FROM AttendanceEvent ae WHERE ae.processingStatus = 'PROCESSED' " +
                     "ORDER BY ae.eventTimestamp DESC")
       Page<AttendanceEvent> findRecentEvents(Pageable pageable);

       /**
        * Get attendance events for a specific employee
        */
       @Query("SELECT ae FROM AttendanceEvent ae WHERE ae.employee.id = ?1 " +
                     "ORDER BY ae.eventTimestamp DESC")
       Page<AttendanceEvent> findByEmployeeId(Long employeeId, Pageable pageable);

       /**
        * Get today's attendance events for an employee
        */
       @Query("SELECT ae FROM AttendanceEvent ae WHERE ae.employee.id = ?1 " +
                     "AND DATE(ae.eventTimestamp) = CURRENT_DATE " +
                     "ORDER BY ae.eventTimestamp DESC")
       List<AttendanceEvent> findTodayByEmployeeId(Long employeeId);

       /**
        * Get fraud/suspicious events for investigation
        */
       @Query("SELECT ae FROM AttendanceEvent ae WHERE ae.isFraudSuspected = true " +
                     "AND ae.processingStatus = 'PROCESSED' " +
                     "ORDER BY ae.eventTimestamp DESC")
       Page<AttendanceEvent> findSuspiciousEvents(Pageable pageable);

       /**
        * Get unverified fraud alerts
        */
       @Query("SELECT ae FROM AttendanceEvent ae WHERE ae.isFraudSuspected = true " +
                     "AND ae.manuallyVerified IS NULL " +
                     "ORDER BY ae.eventTimestamp DESC")
       List<AttendanceEvent> findUnverifiedFraudAlerts();

       /**
        * Get events from a specific device (camera)
        */
       @Query("SELECT ae FROM AttendanceEvent ae WHERE ae.device.id = ?1 " +
                     "ORDER BY ae.eventTimestamp DESC")
       Page<AttendanceEvent> findByDeviceId(Long deviceId, Pageable pageable);

       /**
        * Get attendance events within a time range
        */
       @Query("SELECT ae FROM AttendanceEvent ae WHERE ae.employee.id = ?1 " +
                     "AND ae.eventTimestamp BETWEEN ?2 AND ?3 " +
                     "ORDER BY ae.eventTimestamp DESC")
       List<AttendanceEvent> findByEmployeeAndDateRange(Long employeeId, Instant startTime, Instant endTime);

       /**
        * Count IN events for an employee today (to check if already clocked in)
        */
       @Query("SELECT COUNT(ae) FROM AttendanceEvent ae WHERE ae.employee.id = ?1 " +
                     "AND ae.eventType = 'IN' " +
                     "AND DATE(ae.eventTimestamp) = CURRENT_DATE " +
                     "AND ae.isFraudSuspected = false")
       long countTodayCheckIns(Long employeeId);

       /**
        * Get last event for employee today (to determine if IN or OUT)
        */
       @Query("SELECT ae FROM AttendanceEvent ae WHERE ae.employee.id = ?1 " +
                     "AND DATE(ae.eventTimestamp) = CURRENT_DATE " +
                     "ORDER BY ae.eventTimestamp DESC LIMIT 1")
       AttendanceEvent findLastEventToday(Long employeeId);

       /**
        * Get attendance statistics for a device
        */
       @Query("SELECT COUNT(CASE WHEN ae.eventType = 'IN' THEN 1 END) as checkIns, " +
                     "COUNT(CASE WHEN ae.isFraudSuspected = true THEN 1 END) as suspiciousCount " +
                     "FROM AttendanceEvent ae WHERE ae.device.id = ?1 " +
                     "AND DATE(ae.eventTimestamp) = CURRENT_DATE")
       Object getDeviceStatisticsToday(Long deviceId);

       /**
        * Get unprocessed events (waiting for AI verification)
        */
       @Query("SELECT ae FROM AttendanceEvent ae WHERE ae.processingStatus = 'PENDING' " +
                     "ORDER BY ae.eventTimestamp ASC")
       List<AttendanceEvent> findPendingEvents();

       /**
        * Get failed events (to retry or investigate)
        */
       @Query("SELECT ae FROM AttendanceEvent ae WHERE ae.processingStatus = 'ERROR' " +
                     "ORDER BY ae.eventTimestamp DESC")
       Page<AttendanceEvent> findFailedEvents(Pageable pageable);

       /**
        * Count events by type for dashboard summary
        */
       @Query("SELECT ae.eventType, COUNT(ae) FROM AttendanceEvent ae " +
                     "WHERE DATE(ae.eventTimestamp) = CURRENT_DATE " +
                     "GROUP BY ae.eventType")
       List<Object[]> countEventsByTypeToday();

       /**
        * Get average confidence score for an employee (quality metric)
        */
       @Query("SELECT AVG(ae.confidenceScore) FROM AttendanceEvent ae " +
                     "WHERE ae.employee.id = ?1 AND ae.isFraudSuspected = false")
       Double getAverageConfidenceScore(Long employeeId);

       /**
        * Delete old attendance events (data retention policy)
        */
       void deleteByEventTimestampBefore(Instant cutoffDate);

       /**
        * Get events between timestamps (for BI/analytics)
        */
       @Query("SELECT ae FROM AttendanceEvent ae WHERE ae.eventTimestamp BETWEEN ?1 AND ?2 " +
                     "ORDER BY ae.eventTimestamp DESC")
       List<AttendanceEvent> findByEventTimestampBetweenOrderByEventTimestampDesc(Instant startTime, Instant endTime);

       /**
        * Get events between timestamps ascending order (for hourly analysis)
        */
       @Query("SELECT ae FROM AttendanceEvent ae WHERE ae.eventTimestamp BETWEEN ?1 AND ?2 " +
                     "ORDER BY ae.eventTimestamp ASC")
       List<AttendanceEvent> findByEventTimestampBetweenOrderByEventTimestampAsc(Instant startTime, Instant endTime);

       /**
        * Get department-wise attendance statistics
        * DISABLED: Departement entity does not exist in the system.
        * To re-enable, either create Departement entity with employees relationship,
        * or rewrite query to use actual entity relationships (e.g., via Poste).
        */
       // @Query("SELECT d.id, d.name, " +
       //       "COUNT(DISTINCT e.id) as totalEmployees, " +
       //       "COUNT(DISTINCT CASE WHEN ae.eventType = 'IN' THEN e.id END) as presentToday, " +
       //       "COUNT(DISTINCT e.id) - COUNT(DISTINCT CASE WHEN ae.eventType = 'IN' THEN e.id END) as absentToday, " +
       //       "COUNT(CASE WHEN ae.isFraudSuspected = true THEN 1 END) as suspiciousCount " +
       //       "FROM Departement d " +
       //       "LEFT JOIN d.employees e " +
       //       "LEFT JOIN AttendanceEvent ae ON ae.employee.id = e.id " +
       //       "AND DATE(ae.eventTimestamp) = CURRENT_DATE " +
       //       "WHERE d.id IS NOT NULL " +
       //       "GROUP BY d.id, d.name")
       // List<Object[]> findDepartmentStatistics(java.time.LocalDate startDate, java.time.LocalDate endDate);

       /**
        * Get employee reliability metrics for top unreliable employees
        */
       @Query("SELECT e.id, e.prenom, e.nom, " +
                     "COUNT(CASE WHEN ae.eventType = 'IN' THEN 1 END) as totalCheckIns, " +
                     "COUNT(CASE WHEN ae.isFraudSuspected = true THEN 1 END) as fraudCount " +
                     "FROM Employe e " +
                     "LEFT JOIN AttendanceEvent ae ON ae.employee.id = e.id " +
                     "WHERE ae.processingStatus = 'PROCESSED' " +
                     "GROUP BY e.id, e.prenom, e.nom " +
                     "ORDER BY fraudCount DESC, totalCheckIns ASC")
       List<Object[]> findEmployeeReliabilityMetrics(int limit);
}
