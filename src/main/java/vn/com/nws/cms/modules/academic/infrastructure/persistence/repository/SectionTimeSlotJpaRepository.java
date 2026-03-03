package vn.com.nws.cms.modules.academic.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.com.nws.cms.modules.academic.infrastructure.persistence.entity.SectionTimeSlotEntity;

import java.time.LocalTime;
import java.util.List;

@Repository
public interface SectionTimeSlotJpaRepository extends JpaRepository<SectionTimeSlotEntity, Long> {

    @Query("""
            SELECT ts
            FROM SectionTimeSlotEntity ts
            WHERE ts.section.id = :sectionId
            ORDER BY ts.dayOfWeek, ts.startTime, ts.endTime
            """)
    List<SectionTimeSlotEntity> findBySectionId(@Param("sectionId") Long sectionId);

    @Modifying
    @Query("DELETE FROM SectionTimeSlotEntity ts WHERE ts.section.id = :sectionId")
    void deleteBySectionId(@Param("sectionId") Long sectionId);

    @Query("""
            SELECT CASE WHEN COUNT(ts2) > 0 THEN true ELSE false END
            FROM SectionTimeSlotEntity ts2
            JOIN ts2.section s2
            JOIN EnrollmentEntity e ON e.section.id = s2.id
            WHERE e.student.id = :studentId
              AND s2.semester.id = :semesterId
              AND e.status = vn.com.nws.cms.modules.academic.domain.enums.EnrollmentStatus.ENROLLED
              AND s2.id <> :targetSectionId
              AND ts2.dayOfWeek = :dayOfWeek
              AND ts2.startTime < :endTime
              AND ts2.endTime > :startTime
            """)
    boolean existsStudentScheduleConflict(
            @Param("studentId") Long studentId,
            @Param("semesterId") Long semesterId,
            @Param("targetSectionId") Long targetSectionId,
            @Param("dayOfWeek") short dayOfWeek,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime
    );
}

