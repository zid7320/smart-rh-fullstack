package com.example.hrms.mapper;

import com.example.hrms.dto.EmployeDTO;
import com.example.hrms.model.Employe;
import org.springframework.stereotype.Component;

@Component
public class EmployeMapper {

    public EmployeDTO toDTO(Employe employe) {
        if (employe == null) {
            return null;
        }
        EmployeDTO dto = new EmployeDTO();
        dto.idEmploye = employe.getIdEmploye();
        dto.nom = employe.getNom();
        dto.prenom = employe.getPrenom();
        dto.poste = employe.getPoste();
        dto.email = employe.getEmail();
        return dto;
    }

    public Employe toEntity(EmployeDTO employeDTO) {
        if (employeDTO == null) {
            return null;
        }
        Employe employe = new Employe();
        employe.setIdEmploye(employeDTO.idEmploye);
        employe.setNom(employeDTO.nom);
        employe.setPrenom(employeDTO.prenom);
        employe.setPoste(employeDTO.poste);
        employe.setEmail(employeDTO.email);
        return employe;
    }
}

