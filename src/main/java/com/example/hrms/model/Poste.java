package com.example.hrms.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "postes")
@Data
@NoArgsConstructor
public class Poste {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idPoste;

    @NotBlank(message = "Titre is mandatory")
    private String titre;
    
    @NotBlank(message = "Competences requises is mandatory")
    private String competencesRequises;

    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinTable(name = "poste_competence",
            joinColumns = @JoinColumn(name = "poste_id"),
            inverseJoinColumns = @JoinColumn(name = "competence_id"))
    private Set<Competence> competences = new HashSet<>();

    @OneToMany(mappedBy = "poste", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<Employe> employes = new HashSet<>();
}
