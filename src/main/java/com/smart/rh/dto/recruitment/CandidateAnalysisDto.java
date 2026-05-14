package com.smart.rh.dto.recruitment;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CandidateAnalysisDto {

    private Long id;

    @JsonProperty("job_id")
    private Integer jobId;

    @JsonProperty("candidate_name")
    private String candidateName;

    private String email;

    @JsonProperty("match_score")
    private BigDecimal matchScore;

    @JsonProperty("composite_score")
    private BigDecimal compositeScore;

    @JsonProperty("overall_score")
    private BigDecimal overallScore;

    @JsonProperty("rank_position")
    private Integer rankPosition;

    private String recommendation;

    @JsonProperty("matched_skills")
    private String matchedSkills;

    @JsonProperty("missing_skills")
    private String missingSkills;

    @JsonProperty("experience_match")
    private Boolean experienceMatch;

    @JsonProperty("education_match")
    private Boolean educationMatch;

    private String summary;

    @JsonProperty("total_evaluated")
    private Integer totalEvaluated;

    @JsonProperty("evaluation_timestamp")
    private LocalDateTime evaluationTimestamp;

    @JsonProperty("resume_file_name")
    private String resumeFileName;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;
}
