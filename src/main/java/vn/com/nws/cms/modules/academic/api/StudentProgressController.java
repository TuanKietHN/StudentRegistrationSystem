package vn.com.nws.cms.modules.academic.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import vn.com.nws.cms.common.exception.ResourceNotFoundException;
import vn.com.nws.cms.modules.academic.api.dto.StudentProgressResponse;
import vn.com.nws.cms.modules.academic.application.StudentProgressService;
import vn.com.nws.cms.modules.academic.infrastructure.persistence.entity.StudentClassEntity;
import vn.com.nws.cms.modules.academic.infrastructure.persistence.entity.StudentEntity;
import vn.com.nws.cms.modules.academic.infrastructure.persistence.entity.TeacherEntity;
import vn.com.nws.cms.modules.academic.infrastructure.persistence.repository.StudentJpaRepository;
import vn.com.nws.cms.modules.academic.infrastructure.persistence.repository.TeacherJpaRepository;
import vn.com.nws.cms.modules.auth.infrastructure.persistence.entity.UserEntity;
import vn.com.nws.cms.modules.auth.infrastructure.persistence.repository.JpaUserRepository;

@RestController
@RequestMapping("/api/v1/student-progress")
@RequiredArgsConstructor
@Tag(name = "Academic - Student Progress", description = "API theo dõi tiến độ học tập")
public class StudentProgressController {
    private final StudentProgressService studentProgressService;
    private final JpaUserRepository userRepository;
    private final StudentJpaRepository studentJpaRepository;
    private final TeacherJpaRepository teacherJpaRepository;

    @GetMapping("/{studentId}")
    @PreAuthorize("hasAuthority('student_progress:read_all') or hasAuthority('student_progress:read_class') or hasAuthority('student_progress:read_self')")
    @Operation(summary = "Xem tiến độ học tập của sinh viên")
    public StudentProgressResponse getProgress(@PathVariable Long studentId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        
        // 1. Get current user info
        UserEntity currentUser = userRepository.findByUsername(username)
                .or(() -> userRepository.findByEmail(username))
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        boolean canReadAll = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("student_progress:read_all"));
        
        // 2. Check permissions
        if (!canReadAll) {
            // Find target student
            StudentEntity targetStudent = studentJpaRepository.findById(studentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

            boolean canReadClass = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("student_progress:read_class"));
            boolean canReadSelf = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("student_progress:read_self"));

            if (canReadSelf) {
                // Check if reading self
                StudentEntity currentStudentProfile = studentJpaRepository.findByUserId(currentUser.getId())
                        .orElse(null);
                
                // If user has 'read_self' but is not a student (e.g. mixed role), or target is not self
                if (currentStudentProfile != null && currentStudentProfile.getId().equals(studentId)) {
                    // Allowed
                    return studentProgressService.getStudentProgress(studentId);
                }
            } 
            
            if (canReadClass) {
                // Teacher can only view progress of students in their homeroom class
                TeacherEntity currentTeacherProfile = teacherJpaRepository.findByUserId(currentUser.getId())
                        .orElseThrow(() -> new AccessDeniedException("Teacher profile not found for current user"));
                
                StudentClassEntity studentClass = targetStudent.getStudentClass();
                if (studentClass != null && studentClass.getAdvisorTeacher() != null && 
                    studentClass.getAdvisorTeacher().getId().equals(currentTeacherProfile.getId())) {
                    // Allowed
                    return studentProgressService.getStudentProgress(studentId);
                }
            }
            
            throw new AccessDeniedException("Access denied: You do not have permission to view this student's progress");
        }

        return studentProgressService.getStudentProgress(studentId);
    }
}
