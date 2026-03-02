package vn.com.nws.cms.modules.academic.infrastructure.persistence.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import vn.com.nws.cms.modules.academic.domain.model.AttendanceSession;
import vn.com.nws.cms.modules.academic.domain.repository.AttendanceSessionRepository;
import vn.com.nws.cms.modules.academic.infrastructure.persistence.mapper.AttendanceSessionMapper;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class AttendanceSessionRepositoryImpl implements AttendanceSessionRepository {

    private final AttendanceSessionJpaRepository jpaRepository;
    private final AttendanceSessionMapper mapper;

    @Override
    public AttendanceSession save(AttendanceSession session) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(session)));
    }

    @Override
    public Optional<AttendanceSession> findById(Long id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<AttendanceSession> findByCourseId(Long courseId) {
        return jpaRepository.findByCohortIdOrderBySessionDateDesc(courseId).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public Optional<AttendanceSession> findByCourseIdAndSessionDate(Long courseId, LocalDate sessionDate) {
        return jpaRepository.findFirstByCohortIdAndSessionDate(courseId, sessionDate).map(mapper::toDomain);
    }
}
