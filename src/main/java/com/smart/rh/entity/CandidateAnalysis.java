package com.smart.rh.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "candidate_analysis")
@Getter
@Setter
@ToString
public class CandidateAnalysis extends BaseEntity {

    @Column(name = "job_id")
    private Integer jobId;

    @Column(name = "candidate_name", length = 255)
    private String candidateName;

    @Column(name = "email", length = 255)
    private String email;

    @Column(name = "match_score", precision = 5, scale = 2)
    private BigDecimal matchScore;

    @Column(name = "composite_score", precision = 5, scale = 2)
    private BigDecimal compositeScore;

    @Column(name = "overall_score", precision = 5, scale = 2)
    private BigDecimal overallScore;

    @Column(name = "rank_position")
    private Integer rankPosition;

    @Column(name = "recommendation", length = 100)
    private String recommendation;

    @Column(name = "matched_skills", columnDefinition = "JSON")
    private String matchedSkills;

    @Column(name = "missing_skills", columnDefinition = "JSON")
    private String missingSkills;

    @Column(name = "experience_match")
    private Boolean experienceMatch;

    @Column(name = "education_match")
    private Boolean educationMatch;

    @Column(name = "summary", columnDefinition = "TEXT")
    private String summary;

    @Column(name = "total_evaluated")
    private Integer totalEvaluated;

    @Column(name = "evaluation_timestamp")
    private LocalDateTime evaluationTimestamp;

    @Column(name = "resume_file_name", length = 255)
    private String resumeFileName;
}
