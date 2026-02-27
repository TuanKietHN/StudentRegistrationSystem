package vn.com.nws.cms.modules.academic.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import vn.com.nws.cms.common.exception.BusinessException;
import vn.com.nws.cms.modules.academic.api.dto.*;
import vn.com.nws.cms.modules.academic.domain.enums.CourseLifecycleStatus;
import vn.com.nws.cms.modules.academic.domain.enums.EnrollmentStatus;
import vn.com.nws.cms.modules.academic.domain.model.Course;
import vn.com.nws.cms.modules.academic.domain.model.Enrollment;
import vn.com.nws.cms.modules.academic.domain.model.Student;
import vn.com.nws.cms.modules.academic.domain.repository.CourseRepository;
import vn.com.nws.cms.modules.academic.domain.repository.CourseTimeSlotRepository;
import vn.com.nws.cms.modules.academic.domain.repository.EnrollmentRepository;
import vn.com.nws.cms.modules.academic.domain.repository.StudentRepository;
import vn.com.nws.cms.modules.auth.domain.model.User;
import vn.com.nws.cms.modules.auth.domain.repository.UserRepository;
import vn.com.nws.cms.modules.iam.api.dto.UserResponse;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EnrollmentServiceImpl implements EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final CourseTimeSlotRepository courseTimeSlotRepository;

    /**
     * REQUIRES_NEW: Luôn tạo transaction độc lập, suspend TX cha nếu có.
     *
     * Quan trọng: Method này nhận studentId = student PROFILE id (không phải userId).
     * Lý do: EnrollmentEntity.student là StudentEntity (profile), không phải UserEntity.
     * EnrollmentCreateRequest.studentId phải là Student.id (profile).
     *
     * REQUIRES_NEW đảm bảo khi throw BusinessException:
     * - Chỉ TX con của method này rollback
     * - TX cha (nếu có, ví dụ DataSeeder.run()) KHÔNG bị mark rollback-only
     * - Caller catch exception → tiếp tục bình thường
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public EnrollmentResponse enrollStudent(EnrollmentCreateRequest request) {
        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new BusinessException("Course not found"));

        // request.getStudentId() = student profile id
        Student studentProfile = studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> new BusinessException("Không tìm thấy hồ sơ sinh viên"));

        return enrollInternal(course, studentProfile);
    }

    /**
     * enrollSelf gọi từ API trực tiếp — không cần REQUIRES_NEW.
     * REQUIRED là đủ: không có TX cha nào bị ảnh hưởng.
     */
    @Override
    @Transactional
    public EnrollmentResponse enrollSelf(String username, EnrollmentSelfRequest request) {
        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new BusinessException("Course not found"));
        Student studentProfile = resolveStudentByUsername(username);
        return enrollInternal(course, studentProfile);
    }

    private EnrollmentResponse enrollInternal(Course course, Student studentProfile) {
        // --- Business rule checks ---
        if (!course.isActive()) {
            throw new BusinessException("Course is not active");
        }

        if (course.getStatus() != null && course.getStatus() != CourseLifecycleStatus.OPEN) {
            throw new BusinessException("Course is not open for enrollment");
        }

        assertEnrollmentWindowOpen(course);

        if (!studentProfile.isActive()) {
            throw new BusinessException("Hồ sơ sinh viên không hoạt động");
        }

        // Check trùng enrollment theo studentProfileId — khớp với EnrollmentEntity.student (StudentEntity)
        if (enrollmentRepository.existsByCourseIdAndStudentId(course.getId(), studentProfile.getId())) {
            throw new BusinessException("Student already enrolled in this course");
        }

        if (course.getCurrentStudents() >= course.getMaxStudents()) {
            throw new BusinessException("Course is full");
        }

        // --- Schedule conflict check ---
        var slots = courseTimeSlotRepository.findByCourseId(course.getId());
        if (!slots.isEmpty()) {
            for (var slot : slots) {
                boolean conflict = courseTimeSlotRepository.existsStudentScheduleConflict(
                        studentProfile.getId(),
                        course.getSemester().getId(),
                        course.getId(),
                        slot.getDayOfWeek(),
                        slot.getStartTime(),
                        slot.getEndTime()
                );
                if (conflict) {
                    throw new BusinessException("Trùng lịch với lớp học phần đã đăng ký");
                }
            }
        }

        // --- Persist ---
        course.setCurrentStudents(course.getCurrentStudents() + 1);
        courseRepository.save(course);

        Enrollment enrollment = Enrollment.builder()
                .course(course)
                .student(studentProfile)
                .status(EnrollmentStatus.ENROLLED)
                .build();

        enrollment = enrollmentRepository.save(enrollment);
        return toResponse(enrollment);
    }

    @Override
    @Transactional
    public EnrollmentResponse updateEnrollment(Long id, EnrollmentUpdateRequest request) {
        Enrollment enrollment = enrollmentRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Enrollment not found"));

        if (request.getStatus() != null) {
            if (request.getStatus() == EnrollmentStatus.DROPPED
                    && enrollment.getStatus() != EnrollmentStatus.DROPPED) {
                Course course = enrollment.getCourse();
                course.setCurrentStudents(Math.max(0, course.getCurrentStudents() - 1));
                courseRepository.save(course);
            }
            enrollment.setStatus(request.getStatus());
        }

        if (request.getGrade() != null) {
            enrollment.setGrade(request.getGrade());
        }

        enrollment = enrollmentRepository.save(enrollment);
        return toResponse(enrollment);
    }

    @Override
    @Transactional
    public void cancelEnrollment(Long id, String username, boolean isAdmin) {
        Enrollment enrollment = enrollmentRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Enrollment not found"));

        Course course = enrollment.getCourse();
        assertEnrollmentWindowOpen(course);

        if (!isAdmin) {
            User currentUser = userRepository.findByUsername(username)
                    .or(() -> userRepository.findByEmail(username))
                    .orElseThrow(() -> new BusinessException("User not found"));

            if (enrollment.getStudent() == null
                    || enrollment.getStudent().getUser() == null
                    || enrollment.getStudent().getUser().getId() == null
                    || !enrollment.getStudent().getUser().getId().equals(currentUser.getId())) {
                throw new BusinessException("Bạn không có quyền hủy đăng ký này");
            }
        }

        course.setCurrentStudents(Math.max(0, course.getCurrentStudents() - 1));
        courseRepository.save(course);
        enrollmentRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EnrollmentResponse> getStudentEnrollments(Long studentId) {
        return enrollmentRepository.findByStudentId(studentId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EnrollmentResponse> getMyEnrollments(String username) {
        Student student = resolveStudentByUsername(username);
        return enrollmentRepository.findByStudentId(student.getId()).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EnrollmentResponse> getCourseEnrollments(Long courseId) {
        return enrollmentRepository.findByCourseId(courseId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private EnrollmentResponse toResponse(Enrollment enrollment) {
        Course course    = enrollment.getCourse();
        List<CourseTimeSlotResponse> timeSlots = courseTimeSlotRepository.findByCourseId(course.getId())
                .stream().map(CourseTimeSlotResponse::fromDomain).toList();

        return EnrollmentResponse.builder()
                .id(enrollment.getId())
                .status(enrollment.getStatus())
                .grade(enrollment.getGrade())
                .createdAt(enrollment.getCreatedAt())
                .updatedAt(enrollment.getUpdatedAt())
                .course(CourseResponse.builder()
                        .id(course.getId())
                        .name(course.getName())
                        .code(course.getCode())
                        .maxStudents(course.getMaxStudents())
                        .currentStudents(course.getCurrentStudents())
                        .active(course.isActive())
                        .enrollmentStartDate(course.getEnrollmentStartDate())
                        .enrollmentEndDate(course.getEnrollmentEndDate())
                        .semester(course.getSemester() != null
                                ? SemesterResponse.builder()
                                .id(course.getSemester().getId())
                                .name(course.getSemester().getName())
                                .code(course.getSemester().getCode())
                                .build()
                                : null)
                        .subject(course.getSubject() != null
                                ? SubjectResponse.builder()
                                .id(course.getSubject().getId())
                                .name(course.getSubject().getName())
                                .code(course.getSubject().getCode())
                                .credit(course.getSubject().getCredits())
                                .build()
                                : null)
                        .teacher(course.getTeacher() != null
                                ? UserResponse.builder()
                                .id(course.getTeacher().getId())
                                .username(course.getTeacher().getUsername())
                                .email(course.getTeacher().getEmail())
                                .role(course.getTeacher().getRoles().stream()
                                        .map(Enum::name)
                                        .collect(Collectors.joining(",")))
                                .build()
                                : null)
                        .timeSlots(timeSlots)
                        .build())
                .student(enrollment.getStudent() != null
                        && enrollment.getStudent().getUser() != null
                        ? UserResponse.builder()
                        .id(enrollment.getStudent().getUser().getId())
                        .username(enrollment.getStudent().getUser().getUsername())
                        .email(enrollment.getStudent().getUser().getEmail())
                        .build()
                        : null)
                .build();
    }

    private Student resolveStudentByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .or(() -> userRepository.findByEmail(username))
                .orElseThrow(() -> new BusinessException("User not found"));
        return studentRepository.findByUserId(user.getId())
                .orElseThrow(() -> new BusinessException("Chưa có hồ sơ sinh viên"));
    }

    private void assertEnrollmentWindowOpen(Course course) {
        LocalDate start = course.getEnrollmentStartDate();
        LocalDate end   = course.getEnrollmentEndDate();
        if (start == null || end == null) {
            throw new BusinessException("Ngoài thời gian mở đăng ký");
        }
        LocalDate now = LocalDate.now();
        if (now.isBefore(start) || now.isAfter(end)) {
            throw new BusinessException("Ngoài thời gian mở đăng ký");
        }
    }
}
