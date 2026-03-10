package com.smart.rh.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "formation")
@Getter
@Setter
public class Formation extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employe_id", nullable = false)
    private Employe employe;

    @NotBlank
    @Column(nullable = false, length = 200)
    private String titre;

    @Column(length = 200)
    private String certification;

    @Column(length = 200)
    private String organisme;

    @Column(name = "date_debut")
    private LocalDate dateDebut;

    @Column(name = "date_fin")
    private LocalDate dateFin;
}
