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
import org.springframework.web.bind.annotation.RequestHeader;
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
    @PreAuthorize("hasAnyRole('STUDENT', 'TEACHER', 'ADMIN')")
    @Operation(summary = "Get my schedule")
    public ResponseEntity<List<ScheduleResponse>> getMySchedule(
            @RequestParam(required = false) Long semesterId,
            @RequestHeader(value = "X-Active-Role", required = false) String activeRole
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

        // Determine role based on X-Active-Role header first, then fallback to user roles
        String role = "STUDENT";
        if (activeRole != null && !activeRole.isEmpty()) {
             // Validate if user actually has this role
             boolean hasRole = user.getRoles().stream()
                     .anyMatch(r -> r.name().equals(activeRole));
             if (hasRole) {
                 role = activeRole;
             }
        } else {
            // Fallback logic if no header provided
            if (user.getRoles().stream().anyMatch(r -> r.name().equals("TEACHER"))) {
                role = "TEACHER";
            } else if (user.getRoles().stream().anyMatch(r -> r.name().equals("ADMIN"))) {
                 return ResponseEntity.ok(List.of());
            }
        }
        
        return ResponseEntity.ok(scheduleService.getMySchedule(user.getId(), role, semesterId));
    }
}
