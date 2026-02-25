package com.example.hrms.mapper;

import com.example.hrms.dto.ContratDTO;
import com.example.hrms.model.Contrat;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface ContratMapper {

    ContratDTO toDTO(Contrat contrat);

    Contrat toEntity(ContratDTO contratDTO);
}
