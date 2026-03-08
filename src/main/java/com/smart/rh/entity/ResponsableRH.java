package com.smart.rh.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "responsable_rh")
@Getter
@Setter
public class ResponsableRH extends BaseEntity {

    @NotBlank
    @Column(nullable = false, length = 100)
    private String nom;

    /** Optional link to a User account for this HR manager. */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true)
    private User user;

    @OneToMany(mappedBy = "responsable", fetch = FetchType.LAZY)
    private List<Recrutement> recrutements = new ArrayList<>();
}
