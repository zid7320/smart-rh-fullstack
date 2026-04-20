package com.smart.rh.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * Represents an attendance event (check-in or check-out).
 * Created from RFID card swipe.
 */
@Entity
@Table(name = "attendance_records", indexes = {
        @Index(name = "idx_employee_id", columnList = "employee_id"),
        @Index(name = "idx_event_timestamp", columnList = "event_timestamp"),
        @Index(name = "idx_reader_id", columnList = "rfid_reader_id"),
        @Index(name = "idx_verification_code", columnList = "verification_code"),
        @Index(name = "idx_employee_timestamp", columnList = "employee_id, event_timestamp")
})
@Getter
@Setter
public class AttendanceRecord extends BaseEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employe employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rfid_reader_id")
    private RfidReader rfidReader;

    @Column(name = "rfid_card_id", length = 100)
    private String rfidCardId;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 10)
    private EventType eventType;

    @NotNull
    @Column(name = "event_timestamp", nullable = false)
    private Instant eventTimestamp;

    @Column(name = "location", length = 100)
    private String location;

    @Column(name = "is_verified", nullable = false)
    private Boolean isVerified = false;

    @Column(name = "verification_code", length = 50)
    private String verificationCode;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    public enum EventType {
        IN,
        OUT
    }
}
