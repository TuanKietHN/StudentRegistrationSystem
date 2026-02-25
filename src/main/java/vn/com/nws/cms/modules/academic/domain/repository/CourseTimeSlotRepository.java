package vn.com.nws.cms.modules.academic.domain.repository;

import vn.com.nws.cms.modules.academic.domain.model.CourseTimeSlot;

import java.time.LocalTime;
import java.util.List;

public interface CourseTimeSlotRepository {
    List<CourseTimeSlot> findByCourseId(Long courseId);
    void replaceCourseTimeSlots(Long courseId, List<CourseTimeSlot> slots);

    boolean existsStudentScheduleConflict(Long studentId, Long semesterId, Long targetCourseId, short dayOfWeek, LocalTime startTime, LocalTime endTime);
}

