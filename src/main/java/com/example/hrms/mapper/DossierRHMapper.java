package com.example.hrms.mapper;

import com.example.hrms.dto.DossierRHDTO;
import com.example.hrms.model.DossierRH;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface DossierRHMapper {

    DossierRHDTO toDTO(DossierRH dossierRH);
    DossierRH toEntity(DossierRHDTO dto);
}
