package com.smart.rh.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "recrutement")
@Getter
@Setter
public class Recrutement extends BaseEntity {

    @NotBlank
    @Column(name = "poste_cible", nullable = false, length = 150)
    private String posteCible;

    /**
     * Workflow statut: OUVERT | EN_COURS | CLOTURE | EMBAUCHE
     */
    @Column(nullable = false, length = 50)
    private String statut = "OUVERT";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsable_id")
    private ResponsableRH responsable;

    @OneToMany(mappedBy = "recrutement", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Candidate> candidates = new ArrayList<>();
}
