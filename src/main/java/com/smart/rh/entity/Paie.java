package com.smart.rh.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(
    name = "paie",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_paie_employe_mois_annee",
        columnNames = {"employe_id", "mois", "annee"}
    )
)
@Getter
@Setter
public class Paie extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employe_id", nullable = false)
    private Employe employe;

    @NotNull
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal montant;

    @NotNull
    @Min(1) @Max(12)
    @Column(nullable = false)
    private Integer mois;

    @NotNull
    @Column(nullable = false)
    private Integer annee;

    /** Relative path or URL to the generated PDF bulletin. */
    @Column(name = "bulletin_pdf", length = 500)
    private String bulletinPdf;
}
