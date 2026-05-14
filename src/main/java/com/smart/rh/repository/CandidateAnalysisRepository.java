package com.smart.rh.repository;

import com.smart.rh.entity.CandidateAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CandidateAnalysisRepository extends JpaRepository<CandidateAnalysis, Long> {

    List<CandidateAnalysis> findByJobId(Integer jobId);

    List<CandidateAnalysis> findByEmail(String email);

    @Query("SELECT ca FROM CandidateAnalysis ca WHERE ca.jobId = :jobId ORDER BY ca.overallScore DESC NULLS LAST, ca.rankPosition ASC")
    List<CandidateAnalysis> findByJobIdOrderByScore(@Param("jobId") Integer jobId);

    @Query("SELECT ca FROM CandidateAnalysis ca WHERE ca.jobId = :jobId AND ca.recommendation = :recommendation")
    List<CandidateAnalysis> findByJobIdAndRecommendation(@Param("jobId") Integer jobId, @Param("recommendation") String recommendation);
}
