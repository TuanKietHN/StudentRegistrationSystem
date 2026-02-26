package vn.com.nws.cms.modules.academic.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.com.nws.cms.common.dto.PageResponse;
import vn.com.nws.cms.common.exception.BusinessException;
import vn.com.nws.cms.modules.academic.api.dto.*;
import vn.com.nws.cms.modules.academic.domain.enums.CourseLifecycleStatus;
import vn.com.nws.cms.modules.academic.domain.model.Course;
import vn.com.nws.cms.modules.academic.domain.model.CourseTimeSlot;
import vn.com.nws.cms.modules.academic.domain.model.Semester;
import vn.com.nws.cms.modules.academic.domain.model.Subject;
import vn.com.nws.cms.modules.academic.domain.repository.EnrollmentRepository;
import vn.com.nws.cms.modules.academic.domain.repository.StudentRepository;
import vn.com.nws.cms.modules.academic.domain.repository.CourseRepository;
import vn.com.nws.cms.modules.academic.domain.repository.CourseTimeSlotRepository;
import vn.com.nws.cms.modules.academic.domain.repository.SemesterRepository;
import vn.com.nws.cms.modules.academic.domain.repository.SubjectRepository;
import vn.com.nws.cms.modules.auth.domain.model.User;
import vn.com.nws.cms.modules.auth.domain.repository.UserRepository;
import vn.com.nws.cms.modules.iam.api.dto.UserResponse;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;
    private final SubjectRepository subjectRepository;
    private final SemesterRepository semesterRepository;
    private final UserRepository userRepository;
    private final CourseTimeSlotRepository courseTimeSlotRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final StudentRepository studentRepository;

    @Override
    public PageResponse<CourseResponse> getCourses(CourseFilterRequest request) {
        List<Course> courses = courseRepository.search(
                request.getKeyword(), 
                request.getSemesterId(), 
                request.getSubjectId(), 
                request.getTeacherId(), 
                request.getActive(), 
                request.getStatus(),
                request.getPage(), 
                request.getSize());
        
        long totalElements = courseRepository.count(
                request.getKeyword(), 
                request.getSemesterId(), 
                request.getSubjectId(), 
                request.getTeacherId(), 
                request.getActive(),
                request.getStatus());
        
        int totalPages = (int) Math.ceil((double) totalElements / request.getSize());

        List<CourseResponse> responses = courses.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return PageResponse.<CourseResponse>builder()
                .page(request.getPage())
                .size(request.getSize())
                .totalElements(totalElements)
                .totalPages(totalPages)
                .data(responses)
                .build();
    }

    @Override
    public CourseResponse getCourseById(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Course not found"));
        return toResponse(course);
    }

    @Override
    @Transactional
    public CourseResponse createCourse(CourseCreateRequest request) {
        if (courseRepository.existsByCode(request.getCode())) {
            throw new BusinessException("Course code already exists");
        }

        Subject subject = subjectRepository.findById(request.getSubjectId())
                .orElseThrow(() -> new BusinessException("Subject not found"));

        Semester semester = semesterRepository.findById(request.getSemesterId())
                .orElseThrow(() -> new BusinessException("Semester not found"));

        User teacher = null;
        if (request.getTeacherId() != null) {
            teacher = userRepository.findById(request.getTeacherId())
                    .orElseThrow(() -> new BusinessException("Teacher not found"));
            // Can add role check here if needed
        }

        Course course = Course.builder()
                .name(request.getName())
                .code(request.getCode())
                .maxStudents(request.getMaxStudents())
                .minStudents(request.getMinStudents() != null ? request.getMinStudents() : 0)
                .currentStudents(0)
                .active(request.isActive())
                .status(request.getStatus() != null ? request.getStatus() : CourseLifecycleStatus.OPEN)
                .subject(subject)
                .semester(semester)
                .teacher(teacher)
                .enrollmentStartDate(request.getEnrollmentStartDate() != null ? request.getEnrollmentStartDate() : LocalDate.now())
                .enrollmentEndDate(request.getEnrollmentEndDate() != null ? request.getEnrollmentEndDate() : LocalDate.now().plusDays(365))
                .build();

        validateEnrollmentWindow(course.getEnrollmentStartDate(), course.getEnrollmentEndDate());

        course = courseRepository.save(course);
        return toResponse(course);
    }

    @Override
    @Transactional
    public CourseResponse updateCourse(Long id, CourseUpdateRequest request) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Course not found"));

        if (request.getCode() != null && !request.getCode().equals(course.getCode())) {
            if (courseRepository.existsByCode(request.getCode())) {
                throw new BusinessException("Course code already exists");
            }
            course.setCode(request.getCode());
        }

        if (request.getName() != null) course.setName(request.getName());
        if (request.getMaxStudents() != null) course.setMaxStudents(request.getMaxStudents());
        if (request.getMinStudents() != null) course.setMinStudents(request.getMinStudents());
        if (request.getActive() != null) course.setActive(request.getActive());
        if (request.getStatus() != null) course.setStatus(request.getStatus());
        if (request.getCanceledReason() != null) course.setCanceledReason(request.getCanceledReason());

        if (request.getSubjectId() != null) {
            Subject subject = subjectRepository.findById(request.getSubjectId())
                    .orElseThrow(() -> new BusinessException("Subject not found"));
            course.setSubject(subject);
        }

        if (request.getSemesterId() != null) {
            Semester semester = semesterRepository.findById(request.getSemesterId())
                    .orElseThrow(() -> new BusinessException("Semester not found"));
            course.setSemester(semester);
        }

        if (request.getTeacherId() != null) {
            User teacher = userRepository.findById(request.getTeacherId())
                    .orElseThrow(() -> new BusinessException("Teacher not found"));
            course.setTeacher(teacher);
        }

        if (request.getEnrollmentStartDate() != null) {
            course.setEnrollmentStartDate(request.getEnrollmentStartDate());
        }
        if (request.getEnrollmentEndDate() != null) {
            course.setEnrollmentEndDate(request.getEnrollmentEndDate());
        }
        validateEnrollmentWindow(course.getEnrollmentStartDate(), course.getEnrollmentEndDate());

        course = courseRepository.save(course);
        return toResponse(course);
    }

    @Override
    @Transactional
    public CourseResponse cancelCourse(Long id, CourseCancelRequest request) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Course not found"));

        course.setStatus(CourseLifecycleStatus.CANCELED);
        course.setActive(false);
        course.setCanceledAt(LocalDateTime.now());
        if (request != null && request.getReason() != null && !request.getReason().isBlank()) {
            course.setCanceledReason(request.getReason());
        }

        enrollmentRepository.findByCourseId(id).forEach(e -> enrollmentRepository.deleteById(e.getId()));
        course.setCurrentStudents(0);

        course = courseRepository.save(course);
        return toResponse(course);
    }

    @Override
    @Transactional
    public CourseResponse mergeCourses(CourseMergeRequest request) {
        if (request.getTargetCourseId() == null) {
            throw new BusinessException("Target course is required");
        }
        if (request.getSourceCourseIds() == null || request.getSourceCourseIds().isEmpty()) {
            throw new BusinessException("Source courses are required");
        }

        Set<Long> sourceIds = new HashSet<>(request.getSourceCourseIds());
        sourceIds.remove(request.getTargetCourseId());
        if (sourceIds.isEmpty()) {
            throw new BusinessException("Source courses are required");
        }

        Course target = courseRepository.findById(request.getTargetCourseId())
                .orElseThrow(() -> new BusinessException("Target course not found"));
        if (target.getStatus() != CourseLifecycleStatus.OPEN && target.getStatus() != CourseLifecycleStatus.CLOSED) {
            throw new BusinessException("Target course status is not mergeable");
        }

        var targetSlots = courseTimeSlotRepository.findByCourseId(target.getId());

        int capacity = Math.max(0, target.getMaxStudents() - target.getCurrentStudents());
        int toMove = 0;

        for (Long srcId : sourceIds) {
            Course src = courseRepository.findById(srcId)
                    .orElseThrow(() -> new BusinessException("Source course not found: " + srcId));

            if (!src.getSemester().getId().equals(target.getSemester().getId())) {
                throw new BusinessException("Chỉ được dồn lớp trong cùng học kỳ");
            }
            if (!src.getSubject().getId().equals(target.getSubject().getId())) {
                throw new BusinessException("Chỉ được dồn lớp trong cùng môn học");
            }
            if (src.getStatus() == CourseLifecycleStatus.CANCELED || src.getStatus() == CourseLifecycleStatus.MERGED) {
                throw new BusinessException("Source course status is not mergeable: " + src.getCode());
            }

            var srcSlots = courseTimeSlotRepository.findByCourseId(src.getId());
            if (!sameSchedule(srcSlots, targetSlots)) {
                throw new BusinessException("Chỉ hỗ trợ dồn lớp khi lịch học giống nhau");
            }

            for (var e : enrollmentRepository.findByCourseId(src.getId())) {
                Long studentUserId = e.getStudent().getId();
                Long studentProfileId = studentRepository.findByUserId(studentUserId)
                        .orElseThrow(() -> new BusinessException("Không tìm thấy hồ sơ sinh viên"))
                        .getId();
                boolean already = enrollmentRepository.existsByCourseIdAndStudentId(target.getId(), studentProfileId);
                if (!already) {
                    toMove++;
                }
            }
        }

        if (toMove > capacity) {
            throw new BusinessException("Lớp đích không đủ chỗ để dồn");
        }

        for (Long srcId : sourceIds) {
            Course src = courseRepository.findById(srcId).orElseThrow();
            for (var e : enrollmentRepository.findByCourseId(src.getId())) {
                Long studentUserId = e.getStudent().getId();
                Long studentProfileId = studentRepository.findByUserId(studentUserId)
                        .orElseThrow(() -> new BusinessException("Không tìm thấy hồ sơ sinh viên"))
                        .getId();
                boolean already = enrollmentRepository.existsByCourseIdAndStudentId(target.getId(), studentProfileId);
                if (already) {
                    enrollmentRepository.deleteById(e.getId());
                } else {
                    e.setCourse(target);
                    enrollmentRepository.save(e);
                }
            }

            src.setStatus(CourseLifecycleStatus.MERGED);
            src.setActive(false);
            src.setMergedIntoCourseId(target.getId());
            src.setCanceledAt(LocalDateTime.now());
            if (request.getReason() != null && !request.getReason().isBlank()) {
                src.setCanceledReason(request.getReason());
            }
            src.setCurrentStudents((int) enrollmentRepository.countByCourseId(src.getId()));
            courseRepository.save(src);
        }

        target.setCurrentStudents((int) enrollmentRepository.countByCourseId(target.getId()));
        target = courseRepository.save(target);
        return toResponse(target);
    }

    @Override
    @Transactional
    public void deleteCourse(Long id) {
        if (!courseRepository.findById(id).isPresent()) {
            throw new BusinessException("Course not found");
        }
        courseRepository.deleteById(id);
    }

    private CourseResponse toResponse(Course course) {
        List<CourseTimeSlotResponse> timeSlots = courseTimeSlotRepository.findByCourseId(course.getId()).stream()
                .map(CourseTimeSlotResponse::fromDomain)
                .toList();
        return CourseResponse.builder()
                .id(course.getId())
                .name(course.getName())
                .code(course.getCode())
                .maxStudents(course.getMaxStudents())
                .currentStudents(course.getCurrentStudents())
                .active(course.isActive())
                .status(course.getStatus())
                .minStudents(course.getMinStudents())
                .canceledAt(course.getCanceledAt())
                .canceledReason(course.getCanceledReason())
                .mergedIntoCourseId(course.getMergedIntoCourseId())
                .subject(SubjectResponse.builder()
                        .id(course.getSubject().getId())
                        .name(course.getSubject().getName())
                        .code(course.getSubject().getCode())
                        .credit(course.getSubject().getCredits())
                        .build())
                .semester(SemesterResponse.builder()
                        .id(course.getSemester().getId())
                        .name(course.getSemester().getName())
                        .code(course.getSemester().getCode())
                        .build())
                .teacher(course.getTeacher() != null ? UserResponse.builder()
                        .id(course.getTeacher().getId())
                        .username(course.getTeacher().getUsername())
                        .email(course.getTeacher().getEmail())
                        .role(course.getTeacher().getRoles().stream().map(Enum::name).collect(Collectors.joining(",")))
                        .build() : null)
                .enrollmentStartDate(course.getEnrollmentStartDate())
                .enrollmentEndDate(course.getEnrollmentEndDate())
                .timeSlots(timeSlots)
                .createdAt(course.getCreatedAt())
                .updatedAt(course.getUpdatedAt())
                .build();
    }

    private void validateEnrollmentWindow(LocalDate start, LocalDate end) {
        if (start == null || end == null) {
            throw new BusinessException("Khoảng thời gian mở đăng ký là bắt buộc");
        }
        if (start.isAfter(end)) {
            throw new BusinessException("Thời gian mở đăng ký không hợp lệ");
        }
    }

    private boolean sameSchedule(List<CourseTimeSlot> a, List<CourseTimeSlot> b) {
        if (a == null || a.isEmpty()) return b == null || b.isEmpty();
        if (b == null || b.isEmpty()) return false;
        if (a.size() != b.size()) return false;
        for (int i = 0; i < a.size(); i++) {
            CourseTimeSlot sa = a.get(i);
            CourseTimeSlot sb = b.get(i);
            if (sa.getDayOfWeek() != sb.getDayOfWeek()) return false;
            if (!sa.getStartTime().equals(sb.getStartTime())) return false;
            if (!sa.getEndTime().equals(sb.getEndTime())) return false;
        }
        return true;
    }
}
