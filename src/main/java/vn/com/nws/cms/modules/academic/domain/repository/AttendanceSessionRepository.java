package vn.com.nws.cms.modules.academic.domain.repository;

import vn.com.nws.cms.modules.academic.domain.model.AttendanceSession;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AttendanceSessionRepository {
    AttendanceSession save(AttendanceSession session);
    Optional<AttendanceSession> findById(Long id);
    List<AttendanceSession> findByCourseId(Long courseId);
    Optional<AttendanceSession> findByCourseIdAndSessionDate(Long courseId, LocalDate sessionDate);
}

