package com.example.hrms.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "employes")
@Data
@NoArgsConstructor
public class Employe {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idEmploye;

    @NotBlank(message = "Nom is mandatory")
    private String nom;
    
    @NotBlank(message = "Prenom is mandatory")
    private String prenom;
    
    @NotBlank(message = "Poste is mandatory")
    private String poste;
    
    @Email(message = "Email must be valid")
    @NotBlank(message = "Email is mandatory")
    private String email;

    @OneToMany(mappedBy = "employe", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<Contrat> contrats = new HashSet<>();

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "dossier_id", referencedColumnName = "idDossier")
    private DossierRH dossierRH;

    @OneToMany(mappedBy = "employe", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<Planning> plannings = new HashSet<>();

    @OneToMany(mappedBy = "employe", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<Conge> conges = new HashSet<>();

    @OneToMany(mappedBy = "employe", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<Paie> paies = new HashSet<>();

    @OneToMany(mappedBy = "employe", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<Evaluation> evaluations = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinTable(name = "employe_formation",
            joinColumns = @JoinColumn(name = "employe_id"),
            inverseJoinColumns = @JoinColumn(name = "formation_id"))
    private Set<Formation> formations = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinTable(name = "employe_competence",
            joinColumns = @JoinColumn(name = "employe_id"),
            inverseJoinColumns = @JoinColumn(name = "competence_id"))
    private Set<Competence> competences = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "poste_id")
    private Poste posteOccupe;

    @OneToMany(mappedBy = "employe", fetch = FetchType.LAZY)
    private Set<Recrutement> recrutements = new HashSet<>();
}
