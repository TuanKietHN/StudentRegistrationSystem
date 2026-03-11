package vn.com.nws.cms.modules.academic.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.com.nws.cms.modules.academic.api.dto.StudentProgressResponse;
import vn.com.nws.cms.modules.academic.application.StudentProgressService;

@RestController
@RequestMapping("/api/v1/student-progress")
@RequiredArgsConstructor
@Tag(name = "Academic - Student Progress", description = "API theo dõi tiến độ học tập")
public class StudentProgressController {
    private final StudentProgressService studentProgressService;

    @GetMapping("/{studentId}")
    @PreAuthorize("hasAuthority('STUDENT_PROGRESS:READ_ALL') or hasAuthority('STUDENT_PROGRESS:READ_CLASS') or hasAuthority('STUDENT_PROGRESS:READ_SELF')")
    @Operation(summary = "Xem tiến độ học tập của sinh viên")
    public StudentProgressResponse getProgress(@PathVariable Long studentId) {
        return studentProgressService.getStudentProgress(studentId);
    }
}
