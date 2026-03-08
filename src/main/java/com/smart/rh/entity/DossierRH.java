package com.smart.rh.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "dossier_rh")
@Getter
@Setter
public class DossierRH extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employe_id", nullable = false, unique = true)
    private Employe employe;

    @Column(name = "infos_perso", columnDefinition = "TEXT")
    private String infosPerso;

    @Column(columnDefinition = "TEXT")
    private String diplomes;

    @Column(columnDefinition = "TEXT")
    private String documents;
}
