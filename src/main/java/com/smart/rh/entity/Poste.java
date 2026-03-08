package com.smart.rh.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "poste")
@Getter
@Setter
public class Poste extends BaseEntity {

    @NotBlank
    @Column(nullable = false, length = 150)
    private String titre;

    /** Legacy free-text field; normalised via the poste_competence join table. */
    @Column(name = "competences_requises", columnDefinition = "TEXT")
    private String competencesRequises;

    /**
     * Normalised competence links.
     * Extra column nivel_requis lives only in the DB (see Flyway V2);
     * in JPA we manage the simple many-to-many join.
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "poste_competence",
            joinColumns        = @JoinColumn(name = "poste_id"),
            inverseJoinColumns = @JoinColumn(name = "competence_id")
    )
    private Set<Competence> competences = new HashSet<>();

    @OneToMany(mappedBy = "poste", fetch = FetchType.LAZY)
    private List<Employe> employes = new ArrayList<>();
}
