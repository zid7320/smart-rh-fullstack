package com.smart.rh.mapper;

import com.smart.rh.dto.attendance.AttendanceDto;
import com.smart.rh.dto.attendance.AttendanceEventDto;
import com.smart.rh.dto.attendance.AttendanceRequest;
import com.smart.rh.entity.Attendance;
import com.smart.rh.entity.AttendanceEvent;
import com.smart.rh.entity.AttendanceType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AttendanceMapper {

    @Mapping(source = "employe.id", target = "employeId")
    @Mapping(expression = "java(a.getEmploye().getPrenom() + \" \" + a.getEmploye().getNom())", target = "employeNomComplet")
    @Mapping(source = "type", target = "type", qualifiedByName = "typeToString")
    @Mapping(source = "createdAt", target = "createdAt")
    AttendanceDto toDto(Attendance a);

    /** Reverse mapping used when creating a new Attendance from a REST request. */
    @Mapping(target = "employe", ignore = true) // wired by service
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "rawImagePath", ignore = true) // internal server-side path only
    Attendance toEntity(AttendanceRequest req);

    @Named("typeToString")
    static String typeToString(AttendanceType type) {
        return type != null ? type.name() : null;
    }

    /**
     * Map AttendanceEvent (facial recognition) to DTO
     */
    @Mapping(source = "employee.id", target = "employeeId")
    @Mapping(expression = "java(e.getEmployee().getPrenom() + \" \" + e.getEmployee().getNom())", target = "employeeName")
    @Mapping(source = "device.id", target = "deviceId")
    @Mapping(source = "device.name", target = "deviceName")
    @Mapping(source = "eventType", target = "eventType", qualifiedByName = "eventTypeToString")
    @Mapping(source = "processingStatus", target = "processingStatus", qualifiedByName = "processingStatusToString")
    AttendanceEventDto toAttendanceEventDto(AttendanceEvent e);

    @Named("eventTypeToString")
    static String eventTypeToString(AttendanceEvent.EventType type) {
        return type != null ? type.name() : null;
    }

    @Named("processingStatusToString")
    static String processingStatusToString(AttendanceEvent.ProcessingStatus status) {
        return status != null ? status.name() : null;
    }
}
