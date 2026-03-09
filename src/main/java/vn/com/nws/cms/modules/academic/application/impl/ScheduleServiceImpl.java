package vn.com.nws.cms.modules.academic.application.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.com.nws.cms.common.exception.ResourceNotFoundException;
import vn.com.nws.cms.modules.academic.api.dto.ScheduleResponse;
import vn.com.nws.cms.modules.academic.application.ScheduleService;
import vn.com.nws.cms.modules.academic.infrastructure.persistence.entity.SectionTimeSlotEntity;
import vn.com.nws.cms.modules.academic.infrastructure.persistence.entity.SemesterEntity;
import vn.com.nws.cms.modules.academic.infrastructure.persistence.entity.StudentEntity;
import vn.com.nws.cms.modules.academic.infrastructure.persistence.entity.TeacherEntity;
import vn.com.nws.cms.modules.academic.infrastructure.persistence.repository.JpaSemesterRepository;
import vn.com.nws.cms.modules.academic.infrastructure.persistence.repository.SectionTimeSlotJpaRepository;
import vn.com.nws.cms.modules.academic.infrastructure.persistence.repository.StudentJpaRepository;
import vn.com.nws.cms.modules.academic.infrastructure.persistence.repository.TeacherJpaRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduleServiceImpl implements ScheduleService {

    private final SectionTimeSlotJpaRepository sectionTimeSlotRepository;
    private final StudentJpaRepository studentRepository;
    private final TeacherJpaRepository teacherRepository;
    private final JpaSemesterRepository semesterRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ScheduleResponse> getMySchedule(Long userId, String role, Long semesterId) {
        Long targetSemesterId = semesterId;
        if (targetSemesterId == null) {
            SemesterEntity currentSemester = semesterRepository.findFirstByActiveTrueOrderByStartDateDesc()
                    .orElseThrow(() -> new ResourceNotFoundException("No active semester found"));
            targetSemesterId = currentSemester.getId();
        }

        List<SectionTimeSlotEntity> timeSlots = new ArrayList<>();

        if ("STUDENT".equalsIgnoreCase(role)) {
            StudentEntity student = studentRepository.findByUserId(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Student profile not found for user id: " + userId));
            timeSlots = sectionTimeSlotRepository.findByStudentIdAndSemesterId(student.getId(), targetSemesterId);
        } else if ("TEACHER".equalsIgnoreCase(role)) {
            TeacherEntity teacher = teacherRepository.findByUserId(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Teacher profile not found for user id: " + userId));
            timeSlots = sectionTimeSlotRepository.findByTeacherIdAndSemesterId(teacher.getId(), targetSemesterId);
        }

        return timeSlots.stream().map(this::toScheduleResponse).collect(Collectors.toList());
    }

    private ScheduleResponse toScheduleResponse(SectionTimeSlotEntity entity) {
        String teacherName = "N/A";
        if (entity.getSection().getTeacher() != null && entity.getSection().getTeacher().getUser() != null) {
            teacherName = entity.getSection().getTeacher().getUser().getFullName();
        }

        return ScheduleResponse.builder()
                .sectionId(entity.getSection().getId())
                .sectionName(entity.getSection().getName())
                .subjectName(entity.getSection().getSubject().getName())
                .subjectCode(entity.getSection().getSubject().getCode())
                .teacherName(teacherName)
                .dayOfWeek(entity.getDayOfWeek())
                .startTime(entity.getStartTime())
                .endTime(entity.getEndTime())
                .room(entity.getRoom())
                .build();
    }
}
