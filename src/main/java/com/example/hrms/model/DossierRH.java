package com.example.hrms.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;

@Entity
@Table(name = "dossiers_rh")
@Data
@NoArgsConstructor
public class DossierRH {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idDossier;

    @NotBlank(message = "Personal info is mandatory")
    private String infosPerso;
    
    @NotBlank(message = "Diplomas are mandatory")
    private String diplomes;
    
    @NotBlank(message = "Documents are mandatory")
    private String documents;

    @OneToOne(mappedBy = "dossierRH", fetch = FetchType.LAZY)
    private Employe employe;
}
