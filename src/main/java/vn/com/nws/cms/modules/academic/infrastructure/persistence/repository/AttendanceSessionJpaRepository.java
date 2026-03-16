package vn.com.nws.cms.modules.academic.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.com.nws.cms.modules.academic.infrastructure.persistence.entity.AttendanceSessionEntity;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AttendanceSessionJpaRepository extends JpaRepository<AttendanceSessionEntity, Long> {
    List<AttendanceSessionEntity> findByCohortIdOrderBySessionDateDesc(Long cohortId);
    Optional<AttendanceSessionEntity> findFirstByCohortIdAndSessionDate(Long cohortId, LocalDate sessionDate);
}
