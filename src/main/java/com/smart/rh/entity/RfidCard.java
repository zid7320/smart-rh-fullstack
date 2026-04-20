package com.smart.rh.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

/**
 * Maps an RFID card to an employee.
 * Stores card ID and active status.
 */
@Entity
@Table(name = "rfid_cards", indexes = {
        @Index(name = "idx_employee_id", columnList = "employee_id"),
        @Index(name = "idx_card_id", columnList = "card_id"),
        @Index(name = "idx_is_active", columnList = "is_active")
})
@Getter
@Setter
public class RfidCard extends BaseEntity {

    @NotBlank
    @Column(name = "card_id", nullable = false, length = 100, unique = true)
    private String cardId;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employe employee;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "registered_at", nullable = false)
    private Instant registeredAt = Instant.now();

    @Column(name = "created_by", length = 100)
    private String createdBy;
}
