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

    @Query("""
            SELECT ts
            FROM SectionTimeSlotEntity ts
            JOIN FETCH ts.section s
            JOIN FETCH s.subject
            JOIN FETCH s.teacher t
            LEFT JOIN FETCH t.user
            WHERE t.id = :teacherId
              AND s.semester.id = :semesterId
              AND s.active = true
            ORDER BY ts.dayOfWeek, ts.startTime
            """)
    List<SectionTimeSlotEntity> findByTeacherIdAndSemesterId(@Param("teacherId") Long teacherId, @Param("semesterId") Long semesterId);

    @Query("""
            SELECT ts
            FROM SectionTimeSlotEntity ts
            JOIN FETCH ts.section s
            JOIN FETCH s.subject
            LEFT JOIN FETCH s.teacher t
            LEFT JOIN FETCH t.user
            JOIN EnrollmentEntity e ON e.section.id = s.id
            WHERE e.student.id = :studentId
              AND s.semester.id = :semesterId
              AND e.status = vn.com.nws.cms.modules.academic.domain.enums.EnrollmentStatus.ENROLLED
              AND s.active = true
            ORDER BY ts.dayOfWeek, ts.startTime
            """)
    List<SectionTimeSlotEntity> findByStudentIdAndSemesterId(@Param("studentId") Long studentId, @Param("semesterId") Long semesterId);
}

