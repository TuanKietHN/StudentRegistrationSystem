package vn.com.nws.cms.modules.academic.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.com.nws.cms.common.exception.ResourceNotFoundException;
import vn.com.nws.cms.domain.enums.RoleType;
import vn.com.nws.cms.modules.academic.api.dto.ScheduleResponse;
import vn.com.nws.cms.modules.academic.application.ScheduleService;
import vn.com.nws.cms.modules.auth.domain.model.User;
import vn.com.nws.cms.modules.auth.domain.repository.UserRepository;

import java.util.List;

@RestController
@RequestMapping("/api/v1/schedules")
@RequiredArgsConstructor
@Tag(name = "Schedule", description = "Schedule Management APIs")
public class ScheduleController {

    private final ScheduleService scheduleService;
    private final UserRepository userRepository;

    @GetMapping("/mine")
    @PreAuthorize("hasAnyRole('STUDENT', 'TEACHER')")
    @Operation(summary = "Get my schedule")
    public ResponseEntity<List<ScheduleResponse>> getMySchedule(
            @RequestParam(required = false) Long semesterId
    ) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username;
        Object principal = authentication.getPrincipal();

        if (principal instanceof UserDetails) {
            username = ((UserDetails) principal).getUsername();
        } else {
            username = principal.toString();
        }

        User user = userRepository.findByUsername(username)
                .or(() -> userRepository.findByEmail(username))
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Determine role based on authorities or user roles
        // Assuming user has only one primary role relevant for schedule (STUDENT or TEACHER)
        // If user has both, we might need a way to switch context or return merged schedule?
        // For now, prioritize TEACHER then STUDENT
        String role = RoleType.STUDENT.name();
        if (user.getRoles().contains(RoleType.TEACHER)) {
            role = RoleType.TEACHER.name();
        }

        return ResponseEntity.ok(scheduleService.getMySchedule(user.getId(), role, semesterId));
    }
}
