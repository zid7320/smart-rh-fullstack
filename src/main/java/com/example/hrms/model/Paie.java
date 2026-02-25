package com.example.hrms.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;

@Entity
@Table(name = "paies")
@Data
@NoArgsConstructor
public class Paie {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idPaie;

    @Positive(message = "Amount must be a positive number")
    private Double montant;
    
    @NotBlank(message = "PDF bulletin is mandatory")
    private String bulletinPDF;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employe_id")
    private Employe employe;
}
