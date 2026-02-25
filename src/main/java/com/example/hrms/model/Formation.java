package com.example.hrms.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "formations")
@Data
@NoArgsConstructor
public class Formation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idFormation;

    @NotBlank(message = "Title is mandatory")
    private String titre;
    
    @NotBlank(message = "Certification is mandatory")
    private String certification;

    @ManyToMany(mappedBy = "formations", fetch = FetchType.LAZY)
    private Set<Employe> employes = new HashSet<>();
}
