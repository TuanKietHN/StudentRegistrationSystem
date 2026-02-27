package vn.com.nws.cms.modules.academic.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.com.nws.cms.modules.academic.infrastructure.persistence.entity.CourseTimeSlotEntity;

import java.time.LocalTime;
import java.util.List;

@Repository
public interface CourseTimeSlotJpaRepository extends JpaRepository<CourseTimeSlotEntity, Long> {

    @Query("""
            SELECT ts
            FROM CourseTimeSlotEntity ts
            WHERE ts.course.id = :courseId
            ORDER BY ts.dayOfWeek, ts.startTime, ts.endTime
            """)
    List<CourseTimeSlotEntity> findByCourseId(@Param("courseId") Long courseId);

    @Modifying
    @Query("DELETE FROM CourseTimeSlotEntity ts WHERE ts.course.id = :courseId")
    void deleteByCourseId(@Param("courseId") Long courseId);

    @Query("""
            SELECT CASE WHEN COUNT(ts2) > 0 THEN true ELSE false END
            FROM CourseTimeSlotEntity ts2
            JOIN ts2.course c2
            JOIN EnrollmentEntity e ON e.course.id = c2.id
            WHERE e.student.id = :studentId
              AND c2.semester.id = :semesterId
              AND e.status = vn.com.nws.cms.modules.academic.domain.enums.EnrollmentStatus.ENROLLED
              AND c2.id <> :targetCourseId
              AND ts2.dayOfWeek = :dayOfWeek
              AND ts2.startTime < :endTime
              AND ts2.endTime > :startTime
            """)
    boolean existsStudentScheduleConflict(
            @Param("studentId") Long studentId,
            @Param("semesterId") Long semesterId,
            @Param("targetCourseId") Long targetCourseId,
            @Param("dayOfWeek") short dayOfWeek,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime
    );
}
