package com.smart.rh.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * Audit trail for attendance verification.
 * Tracks who verified/rejected which records.
 */
@Entity
@Table(name = "attendance_verifications", indexes = {
        @Index(name = "idx_status", columnList = "verification_status"),
        @Index(name = "idx_verified_at", columnList = "verified_at")
})
@Getter
@Setter
public class AttendanceVerification extends BaseEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "attendance_record_id", nullable = false)
    private AttendanceRecord attendanceRecord;

    @NotBlank
    @Column(name = "verified_by", nullable = false, length = 100)
    private String verifiedBy;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status", nullable = false, length = 20)
    private VerificationStatus verificationStatus = VerificationStatus.PENDING;

    @Column(name = "rejection_reason", length = 255)
    private String rejectionReason;

    @Column(name = "verified_at")
    private Instant verifiedAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    public enum VerificationStatus {
        PENDING,
        APPROVED,
        REJECTED
    }
}
