package com.example.hrms.dto;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

public class EmployeDTO {
    public Integer idEmploye;
    
    @NotBlank(message = "Nom is mandatory")
    public String nom;
    
    @NotBlank(message = "Prenom is mandatory")
    public String prenom;
    
    @NotBlank(message = "Poste is mandatory")
    public String poste;
    
    @Email(message = "Email must be valid")
    @NotBlank(message = "Email is mandatory")
    public String email;

    // Constructors
    public EmployeDTO() {}

    public EmployeDTO(Integer idEmploye, String nom, String prenom, String poste, String email) {
        this.idEmploye = idEmploye;
        this.nom = nom;
        this.prenom = prenom;
        this.poste = poste;
        this.email = email;
    }

    // Setters for compatibility
    public void setIdEmploye(Integer idEmploye) {
        this.idEmploye = idEmploye;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public void setPoste(String poste) {
        this.poste = poste;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    // Getters for compatibility
    public Integer getIdEmploye() {
        return idEmploye;
    }

    public String getNom() {
        return nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public String getPoste() {
        return poste;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public String toString() {
        return "EmployeDTO{" +
                "idEmploye=" + idEmploye +
                ", nom='" + nom + '\'' +
                ", prenom='" + prenom + '\'' +
                ", poste='" + poste + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}
