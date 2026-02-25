package com.example.hrms.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "recrutements")
@Data
@NoArgsConstructor
public class Recrutement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idRecrutement;

    @NotBlank(message = "Poste cible is mandatory")
    private String posteCible;
    
    @NotBlank(message = "Statut is mandatory")
    private String statut;

    @ManyToMany(mappedBy = "recrutements", fetch = FetchType.LAZY)
    private Set<Candidate> candidates = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsable_id")
    private ResponsableRH responsableRH;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employe_id")
    private Employe employe;
}
