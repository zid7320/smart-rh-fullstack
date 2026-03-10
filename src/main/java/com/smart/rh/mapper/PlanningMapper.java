package com.smart.rh.mapper;

import com.smart.rh.dto.planning.PlanningDto;
import com.smart.rh.dto.planning.PlanningRequest;
import com.smart.rh.entity.Planning;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PlanningMapper {

    @Mapping(source = "employe.id", target = "employeId")
    @Mapping(expression = "java(entity.getEmploye() != null ? entity.getEmploye().getPrenom() + ' ' + entity.getEmploye().getNom() : null)",
             target = "employeNomComplet")
    PlanningDto toDto(Planning entity);

    @Mapping(target = "employe", ignore = true)
    Planning toEntity(PlanningRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "employe", ignore = true)
    void partialUpdate(PlanningRequest request, @MappingTarget Planning entity);

    List<PlanningDto> toDtoList(List<Planning> entities);
}
