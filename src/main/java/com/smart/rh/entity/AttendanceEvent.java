package com.smart.rh.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Attendance event from facial recognition system.
 * Recorded when an employee is detected at an access point (camera/sensor).
 * <p>
 * Stores:
 * - Employee identification (from facial recognition)
 * - Timestamp and location
 * - Recognition confidence score (0-100%)
 * - Event type (IN/OUT/SUSPICIOUS)
 * - Fraud flags (mask detected, spoofing attempt, multiple faces, etc.)
 * </p>
 */
@Entity
@Table(name = "attendance_event", indexes = {
        @Index(name = "idx_employee_timestamp", columnList = "employee_id, event_timestamp DESC"),
        @Index(name = "idx_event_timestamp", columnList = "event_timestamp DESC"),
        @Index(name = "idx_device_timestamp", columnList = "device_id, event_timestamp DESC"),
        @Index(name = "idx_event_type", columnList = "event_type"),
        @Index(name = "idx_is_fraud", columnList = "is_fraud_suspected")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Employee detected by facial recognition
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employe employee;

    /**
     * Camera/Sensor that captured this event
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_id", nullable = false)
    private Device device;

    /**
     * Event type: IN (arrival), OUT (departure), SUSPICIOUS (alert)
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventType eventType;

    /**
     * Timestamp of recognition
     */
    @Column(nullable = false)
    private Instant eventTimestamp;

    /**
     * Facial recognition confidence (0-100%)
     * Higher = more confident match
     */
    @Column(precision = 5, scale = 2)
    private BigDecimal confidenceScore;

    /**
     * Face encoding (optional - can be used for re-verification)
     * Stored as JSON or serialized blob
     */
    @Column(columnDefinition = "LONGTEXT")
    private String faceEmbedding;

    /**
     * Was fraud detected? (mask, spoofing, multiple faces, etc.)
     */
    @Column(nullable = false)
    private Boolean isFraudSuspected;

    /**
     * Reason for fraud suspicion (comma-separated flags)
     * Examples: MASK_DETECTED, SPOOFING_DETECTED, MULTIPLE_FACES,
     * BRIGHTNESS_ABNORMAL
     */
    @Column(length = 500)
    private String fraudReason;

    /**
     * Photo/snapshot from camera at time of recognition
     * Path to stored image or base64 encoded
     */
    @Column(columnDefinition = "LONGTEXT")
    private String photoData;

    /**
     * Processing status: PENDING, PROCESSED, ERROR
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProcessingStatus processingStatus;

    /**
     * Error message if processing failed
     */
    @Column(length = 500)
    private String processingError;

    /**
     * System that processed this event (e.g., "opencv-v2.1", "ai-model-v3")
     */
    @Column(length = 100)
    private String processingSystem;

    /**
     * Processing latency in milliseconds (capture to storage)
     */
    private Long processingTimeMs;

    /**
     * Manual verification by HR/security
     * null = not yet reviewed, true = confirmed valid, false = fraud/rejected
     */
    private Boolean manuallyVerified;

    /**
     * User who manually verified (HR/Security role)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "verified_by_user_id")
    private User verifiedByUser;

    /**
     * Timestamp of manual verification
     */
    private Instant verifiedAt;

    /**
     * Notes from verification
     */
    @Column(columnDefinition = "TEXT")
    private String verificationNotes;

    /**
     * Audit trail
     */
    @CreationTimestamp
    private Instant createdAt;

    @Column(insertable = false, updatable = false)
    private Instant updatedAt;

    /**
     * Event type enumeration
     */
    public enum EventType {
        IN, // Arrival at workplace
        OUT, // Departure from workplace
        SUSPICIOUS // Alert: potential fraud/unauthorized access
    }

    /**
     * Processing status enumeration
     */
    public enum ProcessingStatus {
        PENDING, // Awaiting AI processing
        PROCESSED, // Successfully processed
        ERROR // Error during processing
    }
}
