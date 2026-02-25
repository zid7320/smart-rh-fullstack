package com.example.hrms.mapper;

import com.example.hrms.dto.PlanningDTO;
import com.example.hrms.model.Planning;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-02-11T13:52:54+0100",
    comments = "version: 1.5.3.Final, compiler: Eclipse JDT (IDE) 3.45.0.v20260128-0750, environment: Java 21.0.9 (Eclipse Adoptium)"
)
@Component
public class PlanningMapperImpl implements PlanningMapper {

    @Override
    public PlanningDTO toDTO(Planning planning) {
        if ( planning == null ) {
            return null;
        }

        PlanningDTO planningDTO = new PlanningDTO();

        planningDTO.setHoraires( planning.getHoraires() );
        planningDTO.setIdPlanning( planning.getIdPlanning() );

        return planningDTO;
    }

    @Override
    public Planning toEntity(PlanningDTO dto) {
        if ( dto == null ) {
            return null;
        }

        Planning planning = new Planning();

        planning.setHoraires( dto.getHoraires() );
        planning.setIdPlanning( dto.getIdPlanning() );

        return planning;
    }
}
