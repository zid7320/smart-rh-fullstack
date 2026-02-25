package com.example.hrms.mapper;

import com.example.hrms.dto.EvaluationDTO;
import com.example.hrms.model.Evaluation;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-02-11T13:52:54+0100",
    comments = "version: 1.5.3.Final, compiler: Eclipse JDT (IDE) 3.45.0.v20260128-0750, environment: Java 21.0.9 (Eclipse Adoptium)"
)
@Component
public class EvaluationMapperImpl implements EvaluationMapper {

    @Override
    public EvaluationDTO toDTO(Evaluation evaluation) {
        if ( evaluation == null ) {
            return null;
        }

        EvaluationDTO evaluationDTO = new EvaluationDTO();

        evaluationDTO.setIdEval( evaluation.getIdEval() );
        evaluationDTO.setKpi( evaluation.getKpi() );
        evaluationDTO.setObjectifs( evaluation.getObjectifs() );

        return evaluationDTO;
    }

    @Override
    public Evaluation toEntity(EvaluationDTO dto) {
        if ( dto == null ) {
            return null;
        }

        Evaluation evaluation = new Evaluation();

        evaluation.setIdEval( dto.getIdEval() );
        evaluation.setKpi( dto.getKpi() );
        evaluation.setObjectifs( dto.getObjectifs() );

        return evaluation;
    }
}
