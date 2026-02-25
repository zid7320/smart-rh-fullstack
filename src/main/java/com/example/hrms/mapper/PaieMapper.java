package com.example.hrms.mapper;

import com.example.hrms.dto.PaieDTO;
import com.example.hrms.model.Paie;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface PaieMapper {

    PaieDTO toDTO(Paie paie);
    Paie toEntity(PaieDTO dto);
}
