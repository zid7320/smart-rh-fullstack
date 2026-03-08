package com.smart.rh.mapper;

import com.smart.rh.dto.dossier.DossierRHDto;
import com.smart.rh.dto.dossier.DossierRHRequest;
import com.smart.rh.entity.DossierRH;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DossierRHMapper {

    @Mapping(source = "employe.id",    target = "employeId")
    @Mapping(expression = "java(entity.getEmploye() != null ? entity.getEmploye().getPrenom() + ' ' + entity.getEmploye().getNom() : null)",
             target = "employeNomComplet")
    DossierRHDto toDto(DossierRH entity);

    @Mapping(target = "employe", ignore = true)
    DossierRH toEntity(DossierRHRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "employe", ignore = true)
    void partialUpdate(DossierRHRequest request, @MappingTarget DossierRH entity);

    List<DossierRHDto> toDtoList(List<DossierRH> entities);
}
