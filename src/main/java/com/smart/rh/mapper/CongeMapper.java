package com.smart.rh.mapper;

import com.smart.rh.dto.conge.CongeDto;
import com.smart.rh.dto.conge.CongeRequest;
import com.smart.rh.entity.Conge;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CongeMapper {

    @Mapping(source = "employe.id",    target = "employeId")
    @Mapping(expression = "java(entity.getEmploye() != null ? entity.getEmploye().getPrenom() + ' ' + entity.getEmploye().getNom() : null)",
             target = "employeNomComplet")
    @Mapping(source = "statut",        target = "statut", qualifiedByName = "statutToString")
    @Mapping(source = "decidedBy.id",  target = "decidedByUserId")
    CongeDto toDto(Conge entity);

    @Named("statutToString")
    static String statutToString(com.smart.rh.entity.CongeStatut statut) {
        return statut != null ? statut.name() : null;
    }

    @Mapping(target = "employe",   ignore = true)
    @Mapping(target = "statut",    expression = "java(com.smart.rh.entity.CongeStatut.PENDING)")
    @Mapping(target = "decidedBy", ignore = true)
    @Mapping(target = "decidedAt", ignore = true)
    Conge toEntity(CongeRequest request);

    List<CongeDto> toDtoList(List<Conge> entities);
}
