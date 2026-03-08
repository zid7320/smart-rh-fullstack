package com.smart.rh.mapper;

import com.smart.rh.dto.formation.FormationDto;
import com.smart.rh.dto.formation.FormationRequest;
import com.smart.rh.entity.Formation;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface FormationMapper {

    @Mapping(source = "employe.id", target = "employeId")
    @Mapping(expression = "java(entity.getEmploye() != null ? entity.getEmploye().getPrenom() + ' ' + entity.getEmploye().getNom() : null)",
             target = "employeNomComplet")
    FormationDto toDto(Formation entity);

    @Mapping(target = "employe", ignore = true)
    Formation toEntity(FormationRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "employe", ignore = true)
    void partialUpdate(FormationRequest request, @MappingTarget Formation entity);

    List<FormationDto> toDtoList(List<Formation> entities);
}
