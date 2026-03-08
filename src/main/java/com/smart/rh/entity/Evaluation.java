package com.smart.rh.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "evaluation")
@Getter
@Setter
public class Evaluation extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employe_id", nullable = false)
    private Employe employe;

    @Column(columnDefinition = "TEXT")
    private String objectifs;

    @Column(length = 500)
    private String kpi;

    @Column(name = "date_evaluation")
    private LocalDate dateEvaluation;
}
