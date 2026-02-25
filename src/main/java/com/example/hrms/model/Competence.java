package com.example.hrms.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "competences")
@Data
@NoArgsConstructor
public class Competence {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idCompetence;

    @NotBlank(message = "Skill name is mandatory")
    private String nom;
    
    @NotBlank(message = "Skill level is mandatory")
    private String niveau;

    @ManyToMany(mappedBy = "competences", fetch = FetchType.LAZY)
    private Set<Poste> postes = new HashSet<>();

    @ManyToMany(mappedBy = "competences", fetch = FetchType.LAZY)
    private Set<Employe> employes = new HashSet<>();
}
