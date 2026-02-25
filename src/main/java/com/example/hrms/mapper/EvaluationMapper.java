package com.example.hrms.mapper;

import com.example.hrms.dto.EvaluationDTO;
import com.example.hrms.model.Evaluation;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface EvaluationMapper {

    EvaluationDTO toDTO(Evaluation evaluation);
    Evaluation toEntity(EvaluationDTO dto);
}
