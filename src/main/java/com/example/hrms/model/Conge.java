package com.example.hrms.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PastOrPresent;
import java.util.Date;

@Entity
@Table(name = "conges")
@Data
@NoArgsConstructor
public class Conge {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idConge;

    @NotBlank(message = "Type de conge is mandatory")
    private String type;
    
    @NotNull(message = "Date debut is mandatory")
    @PastOrPresent(message = "Date debut must be in past or present")
    @Temporal(TemporalType.DATE)
    private Date dateDebut;
    
    @NotNull(message = "Date fin is mandatory")
    @Temporal(TemporalType.DATE)
    private Date dateFin;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employe_id")
    private Employe employe;
}
