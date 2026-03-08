package com.smart.rh.mapper;

import com.smart.rh.dto.paie.PaieDto;
import com.smart.rh.dto.paie.PaieRequest;
import com.smart.rh.entity.Paie;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PaieMapper {

    @Mapping(source = "employe.id", target = "employeId")
    @Mapping(expression = "java(entity.getEmploye() != null ? entity.getEmploye().getPrenom() + ' ' + entity.getEmploye().getNom() : null)",
             target = "employeNomComplet")
    PaieDto toDto(Paie entity);

    @Mapping(target = "employe",     ignore = true)
    @Mapping(target = "bulletinPdf", ignore = true)
    Paie toEntity(PaieRequest request);

    List<PaieDto> toDtoList(List<Paie> entities);
}
