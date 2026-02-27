package vn.com.nws.cms.modules.academic.infrastructure.persistence.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;
import vn.com.nws.cms.common.exception.BusinessException;
import vn.com.nws.cms.modules.academic.domain.enums.CourseLifecycleStatus;
import vn.com.nws.cms.modules.academic.domain.model.Course;
import vn.com.nws.cms.modules.academic.domain.repository.CourseRepository;
import vn.com.nws.cms.modules.academic.infrastructure.persistence.entity.CourseEntity;
import vn.com.nws.cms.modules.academic.infrastructure.persistence.mapper.CourseMapper;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class CourseRepositoryImpl implements CourseRepository {

    private final JpaCourseRepository jpaCourseRepository;
    private final JpaTeacherRepository jpaTeacherRepository;
    private final CourseMapper courseMapper;

    @Override
    public Course save(Course course) {
        CourseEntity entity = courseMapper.toEntity(course);
        if (course.getId() != null) {
            entity.setId(course.getId());
        }
        if (course.getTeacher() != null && course.getTeacher().getId() != null) {
            Long teacherUserId = course.getTeacher().getId();
            Long teacherProfileId = jpaTeacherRepository.findByUserId(teacherUserId)
                    .map(t -> t.getId())
                    .orElseThrow(() -> new BusinessException("Teacher profile not found"));
            entity.setTeacherId(teacherProfileId);
        } else {
            entity.setTeacherId(null);
        }
        CourseEntity savedEntity = jpaCourseRepository.save(entity);
        return courseMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Course> findById(Long id) {
        return jpaCourseRepository.findById(id).map(courseMapper::toDomain);
    }

    @Override
    public Optional<Course> findByCode(String code) {
        return jpaCourseRepository.findByCode(code).map(courseMapper::toDomain);
    }

    @Override
    public void deleteById(Long id) {
        jpaCourseRepository.deleteById(id);
    }

    @Override
    public boolean existsByCode(String code) {
        return jpaCourseRepository.existsByCode(code);
    }

    @Override
    public List<Course> search(String keyword, Long semesterId, Long subjectId, Long teacherId, Boolean active, CourseLifecycleStatus status, int page, int size) {
        String normalizedKeyword = keyword == null || keyword.isBlank() ? null : keyword;
        if (normalizedKeyword == null && semesterId == null && subjectId == null && teacherId == null && active == null && status == null) {
            return jpaCourseRepository.findAll(PageRequest.of(page - 1, size)).getContent().stream()
                    .map(courseMapper::toDomain)
                    .collect(Collectors.toList());
        }
        Long teacherProfileId = null;
        if (teacherId != null) {
            teacherProfileId = jpaTeacherRepository.findByUserId(teacherId).map(t -> t.getId()).orElse(null);
            if (teacherProfileId == null) {
                return List.of();
            }
        }
        if (normalizedKeyword == null) {
            return jpaCourseRepository.filter(semesterId, subjectId, teacherProfileId, active, status, PageRequest.of(page - 1, size))
                    .getContent().stream()
                    .map(courseMapper::toDomain)
                    .collect(Collectors.toList());
        }
        return jpaCourseRepository.search(normalizedKeyword, semesterId, subjectId, teacherProfileId, active, status, PageRequest.of(page - 1, size)).getContent().stream()
                .map(courseMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public long count(String keyword, Long semesterId, Long subjectId, Long teacherId, Boolean active, CourseLifecycleStatus status) {
        String normalizedKeyword = keyword == null || keyword.isBlank() ? null : keyword;
        if (normalizedKeyword == null && semesterId == null && subjectId == null && teacherId == null && active == null && status == null) {
            return jpaCourseRepository.count();
        }
        Long teacherProfileId = null;
        if (teacherId != null) {
            teacherProfileId = jpaTeacherRepository.findByUserId(teacherId).map(t -> t.getId()).orElse(null);
            if (teacherProfileId == null) {
                return 0;
            }
        }
        if (normalizedKeyword == null) {
            return jpaCourseRepository.countFilter(semesterId, subjectId, teacherProfileId, active, status);
        }
        return jpaCourseRepository.count(normalizedKeyword, semesterId, subjectId, teacherProfileId, active, status);
    }
}
