package vn.com.nws.cms.modules.academic.domain.repository;

import vn.com.nws.cms.modules.academic.domain.model.AttendanceRecord;

import java.util.List;
import java.util.Optional;

public interface AttendanceRecordRepository {
    AttendanceRecord save(AttendanceRecord record);
    List<AttendanceRecord> saveAll(List<AttendanceRecord> records);
    Optional<AttendanceRecord> findBySessionIdAndEnrollmentId(Long sessionId, Long enrollmentId);
    List<AttendanceRecord> findBySessionId(Long sessionId);
    List<AttendanceRecord> findByCourseId(Long courseId);
}

