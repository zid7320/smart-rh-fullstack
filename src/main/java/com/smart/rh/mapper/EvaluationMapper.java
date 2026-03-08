package com.smart.rh.mapper;

import com.smart.rh.dto.evaluation.EvaluationDto;
import com.smart.rh.dto.evaluation.EvaluationRequest;
import com.smart.rh.entity.Evaluation;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface EvaluationMapper {

    @Mapping(source = "employe.id", target = "employeId")
    @Mapping(expression = "java(entity.getEmploye() != null ? entity.getEmploye().getPrenom() + ' ' + entity.getEmploye().getNom() : null)",
             target = "employeNomComplet")
    EvaluationDto toDto(Evaluation entity);

    @Mapping(target = "employe", ignore = true)
    Evaluation toEntity(EvaluationRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "employe", ignore = true)
    void partialUpdate(EvaluationRequest request, @MappingTarget Evaluation entity);

    List<EvaluationDto> toDtoList(List<Evaluation> entities);
}
