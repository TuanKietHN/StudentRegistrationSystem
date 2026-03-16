package vn.com.nws.cms.modules.academic.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.com.nws.cms.modules.academic.infrastructure.persistence.entity.AttendanceRecordEntity;

import java.util.List;
import java.util.Optional;

public interface AttendanceRecordJpaRepository extends JpaRepository<AttendanceRecordEntity, Long> {
    @Query("SELECT r FROM AttendanceRecordEntity r WHERE r.session.id = :sessionId ORDER BY r.enrollment.id ASC")
    List<AttendanceRecordEntity> findBySessionId(@Param("sessionId") Long sessionId);

    @Query("SELECT r FROM AttendanceRecordEntity r WHERE r.session.id = :sessionId AND r.enrollment.id = :enrollmentId")
    Optional<AttendanceRecordEntity> findBySessionIdAndEnrollmentId(@Param("sessionId") Long sessionId, @Param("enrollmentId") Long enrollmentId);

    @Query("SELECT r FROM AttendanceRecordEntity r JOIN FETCH r.session s WHERE s.cohortId = :cohortId")
    List<AttendanceRecordEntity> findByCohortId(@Param("cohortId") Long cohortId);

    @Modifying
    @Query("DELETE FROM AttendanceRecordEntity r WHERE r.enrollment.id = :enrollmentId")
    void deleteByEnrollmentId(@Param("enrollmentId") Long enrollmentId);
}
