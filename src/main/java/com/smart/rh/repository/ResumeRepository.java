package com.smart.rh.repository;

import com.smart.rh.entity.Resume;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ResumeRepository extends JpaRepository<Resume, Long> {

    List<Resume> findByEmail(String email);

    List<Resume> findBySenderEmail(String senderEmail);

    @Query("SELECT r FROM Resume r WHERE r.receivedAt BETWEEN :startDate AND :endDate ORDER BY r.receivedAt DESC")
    List<Resume> findByReceivedAtBetween(@Param("startDate") LocalDateTime startDate, 
                                         @Param("endDate") LocalDateTime endDate);

    @Query("SELECT r FROM Resume r ORDER BY r.receivedAt DESC")
    List<Resume> findAllOrderByReceivedAtDesc();
}
