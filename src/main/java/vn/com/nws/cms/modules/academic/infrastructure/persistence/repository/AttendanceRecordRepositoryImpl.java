package vn.com.nws.cms.modules.academic.infrastructure.persistence.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import vn.com.nws.cms.common.exception.BusinessException;
import vn.com.nws.cms.modules.academic.domain.enums.AttendanceStatus;
import vn.com.nws.cms.modules.academic.domain.model.AttendanceRecord;
import vn.com.nws.cms.modules.academic.domain.repository.AttendanceRecordRepository;
import vn.com.nws.cms.modules.academic.infrastructure.persistence.entity.AttendanceRecordEntity;
import vn.com.nws.cms.modules.academic.infrastructure.persistence.entity.AttendanceSessionEntity;
import vn.com.nws.cms.modules.academic.infrastructure.persistence.entity.EnrollmentEntity;
import vn.com.nws.cms.modules.academic.infrastructure.persistence.entity.StudentEntity;
import vn.com.nws.cms.modules.academic.infrastructure.persistence.mapper.AttendanceRecordMapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class AttendanceRecordRepositoryImpl implements AttendanceRecordRepository {

    private final AttendanceRecordJpaRepository jpaRepository;
    private final AttendanceSessionJpaRepository sessionJpaRepository;
    private final JpaEnrollmentRepository enrollmentJpaRepository;
    private final StudentJpaRepository studentJpaRepository;
    private final AttendanceRecordMapper mapper;

    @Override
    public AttendanceRecord save(AttendanceRecord record) {
        AttendanceRecordEntity entity = toEntity(record);
        AttendanceRecordEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public List<AttendanceRecord> saveAll(List<AttendanceRecord> records) {
        List<AttendanceRecordEntity> entities = records.stream().map(this::toEntity).toList();
        return jpaRepository.saveAll(entities).stream().map(mapper::toDomain).toList();
    }

    @Override
    public Optional<AttendanceRecord> findBySessionIdAndEnrollmentId(Long sessionId, Long enrollmentId) {
        return jpaRepository.findBySessionIdAndEnrollmentId(sessionId, enrollmentId).map(mapper::toDomain);
    }

    @Override
    public List<AttendanceRecord> findBySessionId(Long sessionId) {
        return jpaRepository.findBySessionId(sessionId).stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<AttendanceRecord> findByCourseId(Long courseId) {
        return jpaRepository.findByCohortId(courseId).stream().map(mapper::toDomain).toList();
    }

    private AttendanceRecordEntity toEntity(AttendanceRecord record) {
        AttendanceSessionEntity session = sessionJpaRepository.findById(record.getSessionId())
                .orElseThrow(() -> new BusinessException("Không tìm thấy buổi điểm danh"));
        EnrollmentEntity enrollment = enrollmentJpaRepository.findById(record.getEnrollmentId())
                .orElseThrow(() -> new BusinessException("Không tìm thấy đăng ký học phần"));
        StudentEntity student = studentJpaRepository.findById(record.getStudentId())
                .orElseThrow(() -> new BusinessException("Không tìm thấy hồ sơ sinh viên"));
        return AttendanceRecordEntity.builder()
                .id(record.getId())
                .session(session)
                .enrollment(enrollment)
                .student(student)
                .status(record.getStatus() != null ? record.getStatus() : AttendanceStatus.ABSENT)
                .markedAt(record.getMarkedAt())
                .note(record.getNote())
                .build();
    }
}
