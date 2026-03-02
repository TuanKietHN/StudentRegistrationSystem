package vn.com.nws.cms.modules.academic.infrastructure.persistence.mapper;

import org.mapstruct.Mapper;
import vn.com.nws.cms.modules.academic.domain.model.AttendanceSession;
import vn.com.nws.cms.modules.academic.infrastructure.persistence.entity.AttendanceSessionEntity;

@Mapper(componentModel = "spring")
public interface AttendanceSessionMapper {
    AttendanceSession toDomain(AttendanceSessionEntity entity);
    AttendanceSessionEntity toEntity(AttendanceSession domain);
}

