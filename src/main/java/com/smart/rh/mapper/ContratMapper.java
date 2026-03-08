package com.smart.rh.mapper;

import com.smart.rh.dto.contrat.ContratDto;
import com.smart.rh.dto.contrat.ContratRequest;
import com.smart.rh.entity.Contrat;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ContratMapper {

    @Mapping(source = "employe.id", target = "employeId")
    @Mapping(expression = "java(entity.getEmploye() != null ? entity.getEmploye().getPrenom() + ' ' + entity.getEmploye().getNom() : null)",
             target = "employeNomComplet")
    ContratDto toDto(Contrat entity);

    @Mapping(target = "employe", ignore = true)
    Contrat toEntity(ContratRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "employe", ignore = true)
    void partialUpdate(ContratRequest request, @MappingTarget Contrat entity);

    List<ContratDto> toDtoList(List<Contrat> entities);
}
