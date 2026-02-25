package vn.com.nws.cms.modules.academic.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.com.nws.cms.common.exception.BusinessException;
import vn.com.nws.cms.modules.academic.api.dto.*;
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

    @Override
    @Transactional
    public EnrollmentResponse enrollStudent(EnrollmentCreateRequest request) {
        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new BusinessException("Course not found"));

        Student studentProfile = studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> new BusinessException("Không tìm thấy hồ sơ sinh viên"));
        return enrollInternal(course, studentProfile);
    }

    @Override
    @Transactional
    public EnrollmentResponse enrollSelf(String username, EnrollmentSelfRequest request) {
        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new BusinessException("Course not found"));
        Student studentProfile = resolveStudentByUsername(username);
        return enrollInternal(course, studentProfile);
    }

    private EnrollmentResponse enrollInternal(Course course, Student studentProfile) {
        if (!course.isActive()) {
            throw new BusinessException("Course is not active");
        }
        assertEnrollmentWindowOpen(course);
        if (!studentProfile.isActive()) {
            throw new BusinessException("Hồ sơ sinh viên không hoạt động");
        }
        if (enrollmentRepository.existsByCourseIdAndStudentId(course.getId(), studentProfile.getId())) {
            throw new BusinessException("Student already enrolled in this course");
        }
        if (course.getCurrentStudents() >= course.getMaxStudents()) {
            throw new BusinessException("Course is full");
        }

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

        // Increase current students count
        course.setCurrentStudents(course.getCurrentStudents() + 1);
        courseRepository.save(course);

        Enrollment enrollment = Enrollment.builder()
                .course(course)
                .student(studentProfile.getUser())
                .status("ENROLLED")
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
            // If dropping, decrease count
            if ("DROPPED".equals(request.getStatus()) && !"DROPPED".equals(enrollment.getStatus())) {
                Course course = enrollment.getCourse();
                course.setCurrentStudents(course.getCurrentStudents() - 1);
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
            if (enrollment.getStudent() == null || enrollment.getStudent().getId() == null
                    || !enrollment.getStudent().getId().equals(currentUser.getId())) {
                throw new BusinessException("Bạn không có quyền hủy đăng ký này");
            }
        }

        // Decrease count
        if (course.getCurrentStudents() > 0) {
            course.setCurrentStudents(course.getCurrentStudents() - 1);
            courseRepository.save(course);
        }

        enrollmentRepository.deleteById(id);
    }

    @Override
    public List<EnrollmentResponse> getStudentEnrollments(Long studentId) {
        return enrollmentRepository.findByStudentId(studentId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<EnrollmentResponse> getMyEnrollments(String username) {
        Student student = resolveStudentByUsername(username);
        return enrollmentRepository.findByStudentId(student.getId()).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<EnrollmentResponse> getCourseEnrollments(Long courseId) {
        return enrollmentRepository.findByCourseId(courseId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private EnrollmentResponse toResponse(Enrollment enrollment) {
        // Simplified mapping for brevity
        return EnrollmentResponse.builder()
                .id(enrollment.getId())
                .status(enrollment.getStatus())
                .grade(enrollment.getGrade())
                .createdAt(enrollment.getCreatedAt())
                .updatedAt(enrollment.getUpdatedAt())
                .course(CourseResponse.builder()
                        .id(enrollment.getCourse().getId())
                        .name(enrollment.getCourse().getName())
                        .code(enrollment.getCourse().getCode())
                        .build())
                .student(UserResponse.builder()
                        .id(enrollment.getStudent().getId())
                        .username(enrollment.getStudent().getUsername())
                        .email(enrollment.getStudent().getEmail())
                        .build())
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
        LocalDate end = course.getEnrollmentEndDate();
        if (start == null || end == null) {
            throw new BusinessException("Ngoài thời gian mở đăng ký");
        }
        LocalDate now = LocalDate.now();
        if (now.isBefore(start) || now.isAfter(end)) {
            throw new BusinessException("Ngoài thời gian mở đăng ký");
        }
    }
}
