package vn.com.nws.cms.modules.academic.application.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.com.nws.cms.common.exception.ResourceNotFoundException;
import vn.com.nws.cms.modules.academic.api.dto.StudentProgressResponse;
import vn.com.nws.cms.modules.academic.api.dto.SubjectProgressDTO;
import vn.com.nws.cms.modules.academic.application.StudentProgressService;
import vn.com.nws.cms.modules.academic.domain.model.*;
import vn.com.nws.cms.modules.academic.domain.repository.EnrollmentRepository;
import vn.com.nws.cms.modules.academic.domain.repository.ProgramSubjectRepository;
import vn.com.nws.cms.modules.academic.domain.repository.StudentRepository;
import vn.com.nws.cms.modules.academic.domain.repository.TeacherRepository;
import vn.com.nws.cms.modules.auth.domain.model.User;
import vn.com.nws.cms.modules.auth.domain.repository.UserRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StudentProgressServiceImpl implements StudentProgressService {
    private final StudentRepository studentRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final ProgramSubjectRepository programSubjectRepository;
    private final TeacherRepository teacherRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public StudentProgressResponse getStudentProgress(Long studentId) {
        // Security Check
        checkPermission(studentId);

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + studentId));

        StudentClass studentClass = student.getStudentClass();
        if (studentClass == null) {
            throw new ResourceNotFoundException("Student is not assigned to any class");
        }

        AcademicProgram program = studentClass.getAcademicProgram();
        
        // Fetch enrollments
        List<Enrollment> enrollments = enrollmentRepository.findByStudentId(studentId);
        
        // Group enrollments by subject code (or ID) and keep the best score
        Map<String, Enrollment> bestEnrollments = enrollments.stream()
                .filter(e -> e.getSection() != null && e.getSection().getSubject() != null)
                .collect(Collectors.toMap(
                        e -> e.getSection().getSubject().getCode(),
                        Function.identity(),
                        (existing, replacement) -> {
                            BigDecimal existingScore = existing.getFinalScore() != null ? existing.getFinalScore() : BigDecimal.ZERO;
                            BigDecimal replacementScore = replacement.getFinalScore() != null ? replacement.getFinalScore() : BigDecimal.ZERO;
                            return existingScore.compareTo(replacementScore) >= 0 ? existing : replacement;
                        }
                ));

        List<SubjectProgressDTO> subjectProgressList = new ArrayList<>();
        int earnedCredits = 0;
        int totalCredits = 0;
        
        double totalWeightedScore10 = 0.0;
        double totalWeightedScore4 = 0.0;
        int totalCreditsForGpa = 0;

        if (program != null) {
            List<ProgramSubject> programSubjects = programSubjectRepository.findByProgramId(program.getId());
            totalCredits = program.getTotalCredits() != null ? program.getTotalCredits() : 0;

            for (ProgramSubject ps : programSubjects) {
                Subject subject = ps.getSubject();
                Enrollment enrollment = bestEnrollments.get(subject.getCode());
                
                SubjectProgressDTO dto = buildSubjectProgressDTO(subject, enrollment, ps.getPassScore());
                subjectProgressList.add(dto);

                // Calculate progress
                if ("PASSED".equals(dto.getStatus())) {
                    earnedCredits += subject.getCredits();
                }
            }
            
            // Add extra subjects not in program (optional, maybe electives outside plan)
            // For now let's stick to program subjects
            
        } else {
            // No program defined, just list all enrollments
            for (Enrollment enrollment : bestEnrollments.values()) {
                Subject subject = enrollment.getSection().getSubject();
                SubjectProgressDTO dto = buildSubjectProgressDTO(subject, enrollment, 4.0); // Default pass score
                subjectProgressList.add(dto);
                
                if ("PASSED".equals(dto.getStatus())) {
                    earnedCredits += subject.getCredits();
                }
            }
            // Use sum of subject credits as total? No, maybe 0.
        }
        
        // Calculate GPA based on ALL passed subjects (or all attempted?)
        // Usually GPA includes failed subjects too depending on policy.
        // Let's assume GPA is calculated on all attempted subjects with a score.
        for (Enrollment enrollment : bestEnrollments.values()) {
             if (enrollment.getFinalScore() != null) {
                 Subject subject = enrollment.getSection().getSubject();
                 int credits = subject.getCredits();
                 double score10 = enrollment.getFinalScore().doubleValue();
                 double score4 = convertToGPA4(score10);
                 
                 totalWeightedScore10 += score10 * credits;
                 totalWeightedScore4 += score4 * credits;
                 totalCreditsForGpa += credits;
             }
        }

        double gpa10 = totalCreditsForGpa > 0 ? totalWeightedScore10 / totalCreditsForGpa : 0.0;
        double gpa4 = totalCreditsForGpa > 0 ? totalWeightedScore4 / totalCreditsForGpa : 0.0;
        
        double completionRate = (totalCredits > 0) 
                ? ((double) earnedCredits / totalCredits) * 100 
                : 0.0;

        return StudentProgressResponse.builder()
                .studentId(student.getId())
                .studentName(student.getUser().getFullName()) // Assuming User has fullName
                .programName(program != null ? program.getName() : (studentClass.getProgram() != null ? studentClass.getProgram() : "N/A"))
                .progressPercentage(round(completionRate, 2))
                .totalCredits(totalCredits)
                .earnedCredits(earnedCredits)
                .gpa10(round(gpa10, 2))
                .gpa4(round(gpa4, 2))
                .subjects(subjectProgressList)
                .build();
    }

    private SubjectProgressDTO buildSubjectProgressDTO(Subject subject, Enrollment enrollment, Double passScore) {
        Double score10 = null;
        String letterGrade = null;
        Double grade4 = null;
        String status = "NOT_STARTED";
        
        if (enrollment != null && enrollment.getFinalScore() != null) {
            score10 = enrollment.getFinalScore().doubleValue();
            grade4 = convertToGPA4(score10);
            letterGrade = convertToLetterGrade(score10);
            
            double effectivePassScore = passScore != null ? passScore : 4.0;
            if (score10 >= effectivePassScore) {
                status = "PASSED";
            } else {
                status = "NOT_PASSED";
            }
        } else if (enrollment != null) {
            status = "IN_PROGRESS";
        }

        return SubjectProgressDTO.builder()
                .subjectCode(subject.getCode())
                .subjectName(subject.getName())
                .credits(subject.getCredits())
                .finalScore(score10)
                .grade4(grade4)
                .letterGrade(letterGrade)
                .status(status)
                .type("COMPULSORY") // Simplified, should come from ProgramSubject
                .build();
    }

    private double convertToGPA4(double score10) {
        if (score10 >= 8.5) return 4.0;
        if (score10 >= 7.0) return 3.0;
        if (score10 >= 5.5) return 2.0;
        if (score10 >= 4.0) return 1.0;
        return 0.0;
    }

    private String convertToLetterGrade(double score10) {
        if (score10 >= 8.5) return "A";
        if (score10 >= 7.0) return "B";
        if (score10 >= 5.5) return "C";
        if (score10 >= 4.0) return "D";
        return "F";
    }
    
    private double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();
        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    private void checkPermission(Long targetStudentId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new AccessDeniedException("User is not authenticated");
        }

        boolean canReadAll = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("STUDENT_PROGRESS:READ_ALL"));
        if (canReadAll) {
            return;
        }

        String username = auth.getName();
        User currentUser = userRepository.findByUsername(username)
                .or(() -> userRepository.findByEmail(username))
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        boolean canReadClass = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("STUDENT_PROGRESS:READ_CLASS"));
        boolean canReadSelf = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("STUDENT_PROGRESS:READ_SELF"));

        if (canReadSelf) {
            Student currentStudentProfile = studentRepository.findByUserId(currentUser.getId()).orElse(null);
            if (currentStudentProfile != null && currentStudentProfile.getId().equals(targetStudentId)) {
                return;
            }
        }

        if (canReadClass) {
            Teacher currentTeacherProfile = teacherRepository.findByUserId(currentUser.getId())
                    .orElseThrow(() -> new AccessDeniedException("Teacher profile not found for current user"));

            Student targetStudent = studentRepository.findById(targetStudentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
            
            StudentClass studentClass = targetStudent.getStudentClass();
            if (studentClass != null && studentClass.getAdvisorTeacher() != null &&
                    studentClass.getAdvisorTeacher().getId().equals(currentTeacherProfile.getId())) {
                return;
            }
        }

        throw new AccessDeniedException("Access denied: You do not have permission to view this student's progress");
    }
}
