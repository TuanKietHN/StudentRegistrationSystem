package vn.com.nws.cms.modules.academic.infrastructure.persistence.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import vn.com.nws.cms.modules.academic.domain.model.CourseTimeSlot;
import vn.com.nws.cms.modules.academic.domain.repository.CourseTimeSlotRepository;
import vn.com.nws.cms.modules.academic.infrastructure.persistence.entity.CourseEntity;
import vn.com.nws.cms.modules.academic.infrastructure.persistence.entity.CourseTimeSlotEntity;
import vn.com.nws.cms.modules.academic.infrastructure.persistence.mapper.CourseTimeSlotMapper;

import java.time.LocalTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class CourseTimeSlotRepositoryImpl implements CourseTimeSlotRepository {

    private final CourseTimeSlotJpaRepository jpaRepository;
    private final JpaCourseRepository courseJpaRepository;
    private final CourseTimeSlotMapper mapper;

    @Override
    public List<CourseTimeSlot> findByCourseId(Long courseId) {
        return jpaRepository.findByCourseId(courseId).stream().map(mapper::toDomain).toList();
    }

    @Override
    @Transactional
    public void replaceCourseTimeSlots(Long courseId, List<CourseTimeSlot> slots) {
        jpaRepository.deleteByCourseId(courseId);
        if (slots == null || slots.isEmpty()) {
            return;
        }
        CourseEntity course = courseJpaRepository.findById(courseId).orElse(null);
        if (course == null) {
            return;
        }
        for (CourseTimeSlot slot : slots) {
            CourseTimeSlotEntity entity = mapper.toEntity(slot);
            entity.setCourse(course);
            jpaRepository.save(entity);
        }
    }

    @Override
    public boolean existsStudentScheduleConflict(Long studentId, Long semesterId, Long targetCourseId, short dayOfWeek, LocalTime startTime, LocalTime endTime) {
        return jpaRepository.existsStudentScheduleConflict(studentId, semesterId, targetCourseId, dayOfWeek, startTime, endTime);
    }
}
