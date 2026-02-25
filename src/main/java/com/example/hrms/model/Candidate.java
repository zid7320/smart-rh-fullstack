package com.example.hrms.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "candidates")
@Data
@NoArgsConstructor
public class Candidate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idCandidate;

    @NotBlank(message = "Nom is mandatory")
    private String nom;
    
    @NotBlank(message = "Prenom is mandatory")
    private String prenom;
    
    @Email(message = "Email must be valid")
    @NotBlank(message = "Email is mandatory")
    private String email;

    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinTable(name = "candidate_recrutement",
            joinColumns = @JoinColumn(name = "candidate_id"),
            inverseJoinColumns = @JoinColumn(name = "recrutement_id"))
    private Set<Recrutement> recrutements = new HashSet<>();
}
