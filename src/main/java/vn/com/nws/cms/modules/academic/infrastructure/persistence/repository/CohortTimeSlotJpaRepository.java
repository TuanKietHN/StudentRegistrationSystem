package vn.com.nws.cms.modules.academic.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.com.nws.cms.modules.academic.infrastructure.persistence.entity.CohortTimeSlotEntity;

import java.time.LocalTime;
import java.util.List;

@Repository
public interface CohortTimeSlotJpaRepository extends JpaRepository<CohortTimeSlotEntity, Long> {

    @Query("""
            SELECT ts
            FROM CohortTimeSlotEntity ts
            WHERE ts.cohort.id = :cohortId
            ORDER BY ts.dayOfWeek, ts.startTime, ts.endTime
            """)
    List<CohortTimeSlotEntity> findByCohortId(@Param("cohortId") Long cohortId);

    @Modifying
    @Query("DELETE FROM CohortTimeSlotEntity ts WHERE ts.cohort.id = :cohortId")
    void deleteByCohortId(@Param("cohortId") Long cohortId);

    @Query("""
            SELECT CASE WHEN COUNT(ts2) > 0 THEN true ELSE false END
            FROM CohortTimeSlotEntity ts2
            JOIN ts2.cohort c2
            JOIN EnrollmentEntity e ON e.cohort.id = c2.id
            WHERE e.student.id = :studentId
              AND c2.semester.id = :semesterId
              AND e.status = vn.com.nws.cms.modules.academic.domain.enums.EnrollmentStatus.ENROLLED
              AND c2.id <> :targetCohortId
              AND ts2.dayOfWeek = :dayOfWeek
              AND ts2.startTime < :endTime
              AND ts2.endTime > :startTime
            """)
    boolean existsStudentScheduleConflict(
            @Param("studentId") Long studentId,
            @Param("semesterId") Long semesterId,
            @Param("targetCohortId") Long targetCohortId,
            @Param("dayOfWeek") short dayOfWeek,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime
    );
}

