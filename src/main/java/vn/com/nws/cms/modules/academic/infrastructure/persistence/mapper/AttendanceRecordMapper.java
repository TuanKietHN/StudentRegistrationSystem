package vn.com.nws.cms.modules.academic.infrastructure.persistence.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import vn.com.nws.cms.modules.academic.domain.model.AttendanceRecord;
import vn.com.nws.cms.modules.academic.infrastructure.persistence.entity.AttendanceRecordEntity;

@Mapper(componentModel = "spring")
public interface AttendanceRecordMapper {
    @Mapping(target = "sessionId", source = "session.id")
    @Mapping(target = "enrollmentId", source = "enrollment.id")
    @Mapping(target = "studentId", source = "student.id")
    AttendanceRecord toDomain(AttendanceRecordEntity entity);
}

