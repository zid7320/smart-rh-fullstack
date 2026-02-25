package com.example.hrms.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.util.Date;

@Entity
@Table(name = "contrats")
@Data
@NoArgsConstructor
public class Contrat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idContrat;

    @NotBlank(message = "Type de contrat is mandatory")
    private String type;
    
    @NotNull(message = "Date debut is mandatory")
    @Temporal(TemporalType.DATE)
    private Date dateDebut;
    
    @NotNull(message = "Date fin is mandatory")
    @Temporal(TemporalType.DATE)
    private Date dateFin;
    
    @Positive(message = "Salaire must be positive")
    @NotNull(message = "Salaire is mandatory")
    private Double salaire;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employe_id")
    private Employe employe;
}
