package com.smart.rh.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "competence")
@Getter
@Setter
public class Competence extends BaseEntity {

    @NotBlank
    @Column(nullable = false, length = 100)
    private String nom;

    @Column(length = 50)
    private String niveau;

    @ManyToMany(mappedBy = "competences", fetch = FetchType.LAZY)
    private Set<Poste> postes = new HashSet<>();
}
