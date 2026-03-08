package com.smart.rh.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "contrat")
@Getter
@Setter
public class Contrat extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employe_id", nullable = false)
    private Employe employe;

    /** CDI, CDD, STAGE, INTERIM, … */
    @NotBlank
    @Column(nullable = false, length = 50)
    private String type;

    @NotNull
    @Column(name = "date_debut", nullable = false)
    private LocalDate dateDebut;

    /** Null for CDI (open-ended contracts). */
    @Column(name = "date_fin")
    private LocalDate dateFin;

    @NotNull
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal salaire;
}
