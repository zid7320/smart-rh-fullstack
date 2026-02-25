package com.example.hrms.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "responsables_rh")
@Data
@NoArgsConstructor
public class ResponsableRH {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idResponsable;

    @NotBlank(message = "Nom is mandatory")
    private String nom;

    @OneToMany(mappedBy = "responsableRH", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Recrutement> recrutements = new ArrayList<>();
}
