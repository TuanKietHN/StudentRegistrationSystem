package vn.com.nws.cms.modules.academic.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.com.nws.cms.common.dto.PageResponse;
import vn.com.nws.cms.common.exception.BusinessException;
import vn.com.nws.cms.modules.academic.api.dto.*;
import vn.com.nws.cms.modules.academic.domain.enums.SectionLifecycleStatus;
import vn.com.nws.cms.modules.academic.domain.model.*;
import vn.com.nws.cms.modules.academic.domain.repository.*;
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
public class SectionServiceImpl implements SectionService {

    private final SectionRepository sectionRepository;
    private final SubjectRepository subjectRepository;
    private final SemesterRepository semesterRepository;
    private final UserRepository userRepository;
    private final SectionTimeSlotRepository sectionTimeSlotRepository;
    private final EnrollmentRepository enrollmentRepository;

    @Override
    public PageResponse<SectionResponse> getSections(SectionFilterRequest request) {
        String keyword = request.getKeyword() == null ? "" : request.getKeyword().trim();
        List<Section> sections = sectionRepository.search(
                keyword,
                request.getSemesterId(),
                request.getSubjectId(),
                request.getTeacherId(),
                request.getActive(),
                request.getStatus(),
                request.getPage(),
                request.getSize());

        long totalElements = sectionRepository.count(
                keyword,
                request.getSemesterId(),
                request.getSubjectId(),
                request.getTeacherId(),
                request.getActive(),
                request.getStatus());

        int totalPages = (int) Math.ceil((double) totalElements / request.getSize());

        List<SectionResponse> responses = sections.stream().map(this::toResponse).collect(Collectors.toList());

        return PageResponse.<SectionResponse>builder()
                .page(request.getPage())
                .size(request.getSize())
                .totalElements(totalElements)
                .totalPages(totalPages)
                .data(responses)
                .build();
    }

    @Override
    public SectionResponse getSectionById(Long id) {
        Section section = sectionRepository.findById(id).orElseThrow(() -> new BusinessException("Section not found"));
        return toResponse(section);
    }

    @Override
    @Transactional
    public SectionResponse createSection(SectionCreateRequest request) {
        if (sectionRepository.existsByCode(request.getCode())) {
            throw new BusinessException("Section code already exists");
        }

        Subject subject = subjectRepository.findById(request.getSubjectId())
                .orElseThrow(() -> new BusinessException("Subject not found"));

        Semester semester = semesterRepository.findById(request.getSemesterId())
                .orElseThrow(() -> new BusinessException("Semester not found"));

        User teacher = null;
        if (request.getTeacherId() != null) {
            teacher = userRepository.findById(request.getTeacherId())
                    .orElseThrow(() -> new BusinessException("Teacher not found"));
        }

        Section section = Section.builder()
                .name(request.getName())
                .code(request.getCode())
                .maxStudents(request.getMaxStudents())
                .minStudents(request.getMinStudents() != null ? request.getMinStudents() : 0)
                .currentStudents(0)
                .active(request.isActive())
                .status(request.getStatus() != null ? request.getStatus() : SectionLifecycleStatus.OPEN)
                .subject(subject)
                .semester(semester)
                .teacher(teacher)
                .enrollmentStartDate(request.getEnrollmentStartDate() != null ? request.getEnrollmentStartDate() : LocalDate.now())
                .enrollmentEndDate(request.getEnrollmentEndDate() != null ? request.getEnrollmentEndDate() : LocalDate.now().plusDays(365))
                .registrationEnabled(request.isRegistrationEnabled())
                .build();

        validateEnrollmentWindow(section.getEnrollmentStartDate(), section.getEnrollmentEndDate());
        section = sectionRepository.save(section);
        return toResponse(section);
    }

    @Override
    @Transactional
    public SectionResponse updateSection(Long id, SectionUpdateRequest request) {
        Section section = sectionRepository.findById(id).orElseThrow(() -> new BusinessException("Section not found"));

        if (request.getCode() != null && !request.getCode().equals(section.getCode())) {
            if (sectionRepository.existsByCode(request.getCode())) {
                throw new BusinessException("Section code already exists");
            }
            section.setCode(request.getCode());
        }

        if (request.getName() != null) section.setName(request.getName());
        if (request.getMaxStudents() != null) section.setMaxStudents(request.getMaxStudents());
        if (request.getMinStudents() != null) section.setMinStudents(request.getMinStudents());
        if (request.getActive() != null) section.setActive(request.getActive());
        if (request.getStatus() != null) section.setStatus(request.getStatus());
        if (request.getCanceledReason() != null) section.setCanceledReason(request.getCanceledReason());

        if (request.getSubjectId() != null) {
            Subject subject = subjectRepository.findById(request.getSubjectId())
                    .orElseThrow(() -> new BusinessException("Subject not found"));
            section.setSubject(subject);
        }

        if (request.getSemesterId() != null) {
            Semester semester = semesterRepository.findById(request.getSemesterId())
                    .orElseThrow(() -> new BusinessException("Semester not found"));
            section.setSemester(semester);
        }

        if (request.getTeacherId() != null) {
            User teacher = userRepository.findById(request.getTeacherId())
                    .orElseThrow(() -> new BusinessException("Teacher not found"));
            section.setTeacher(teacher);
        }

        if (request.getEnrollmentStartDate() != null) {
            section.setEnrollmentStartDate(request.getEnrollmentStartDate());
        }
        if (request.getEnrollmentEndDate() != null) {
            section.setEnrollmentEndDate(request.getEnrollmentEndDate());
        }
        if (request.getRegistrationEnabled() != null) {
            section.setRegistrationEnabled(request.getRegistrationEnabled());
        }
        validateEnrollmentWindow(section.getEnrollmentStartDate(), section.getEnrollmentEndDate());

        section = sectionRepository.save(section);
        return toResponse(section);
    }

    @Override
    @Transactional
    public SectionResponse cancelSection(Long id, SectionCancelRequest request) {
        Section section = sectionRepository.findById(id).orElseThrow(() -> new BusinessException("Section not found"));

        section.setStatus(SectionLifecycleStatus.CANCELED);
        section.setActive(false);
        section.setCanceledAt(LocalDateTime.now());
        if (request != null && request.getReason() != null && !request.getReason().isBlank()) {
            section.setCanceledReason(request.getReason());
        }

        enrollmentRepository.findBySectionId(id).forEach(e -> enrollmentRepository.deleteById(e.getId()));
        section.setCurrentStudents(0);

        section = sectionRepository.save(section);
        return toResponse(section);
    }

    @Override
    @Transactional
    public SectionResponse mergeSections(SectionMergeRequest request) {
        if (request.getTargetSectionId() == null) {
            throw new BusinessException("Target section is required");
        }
        if (request.getSourceSectionIds() == null || request.getSourceSectionIds().isEmpty()) {
            throw new BusinessException("Source sections are required");
        }

        Set<Long> sourceIds = new HashSet<>(request.getSourceSectionIds());
        sourceIds.remove(request.getTargetSectionId());
        if (sourceIds.isEmpty()) {
            throw new BusinessException("Source sections are required");
        }

        Section target = sectionRepository.findById(request.getTargetSectionId())
                .orElseThrow(() -> new BusinessException("Target section not found"));
        if (target.getStatus() != SectionLifecycleStatus.OPEN && target.getStatus() != SectionLifecycleStatus.CLOSED) {
            throw new BusinessException("Target section status is not mergeable");
        }

        var targetSlots = sectionTimeSlotRepository.findBySectionId(target.getId());

        int capacity = Math.max(0, target.getMaxStudents() - target.getCurrentStudents());
        int toMove = 0;

        for (Long srcId : sourceIds) {
            Section src = sectionRepository.findById(srcId)
                    .orElseThrow(() -> new BusinessException("Source section not found: " + srcId));

            if (!src.getSemester().getId().equals(target.getSemester().getId())) {
                throw new BusinessException("Chỉ được dồn lớp trong cùng học kỳ");
            }
            if (!src.getSubject().getId().equals(target.getSubject().getId())) {
                throw new BusinessException("Chỉ được dồn lớp trong cùng môn");
            }
            if (src.getStatus() == SectionLifecycleStatus.CANCELED || src.getStatus() == SectionLifecycleStatus.MERGED) {
                throw new BusinessException("Source section status is not mergeable: " + src.getCode());
            }

            var srcSlots = sectionTimeSlotRepository.findBySectionId(src.getId());
            if (!sameSchedule(srcSlots, targetSlots)) {
                throw new BusinessException("Chỉ hỗ trợ dồn lớp khi lịch học giống nhau");
            }

            for (var e : enrollmentRepository.findBySectionId(src.getId())) {
                Long studentProfileId = e.getStudent() != null ? e.getStudent().getId() : null;
                if (studentProfileId == null) {
                    continue;
                }
                boolean already = enrollmentRepository.existsBySectionIdAndStudentId(target.getId(), studentProfileId);
                if (!already) {
                    toMove++;
                }
            }
        }

        if (toMove > capacity) {
            throw new BusinessException("Lớp đích không đủ chỗ để dồn");
        }

        for (Long srcId : sourceIds) {
            Section src = sectionRepository.findById(srcId).orElseThrow();
            for (var e : enrollmentRepository.findBySectionId(src.getId())) {
                Long studentProfileId = e.getStudent() != null ? e.getStudent().getId() : null;
                if (studentProfileId == null) {
                    enrollmentRepository.deleteById(e.getId());
                    continue;
                }
                boolean already = enrollmentRepository.existsBySectionIdAndStudentId(target.getId(), studentProfileId);
                if (already) {
                    enrollmentRepository.deleteById(e.getId());
                } else {
                    e.setSection(target);
                    enrollmentRepository.save(e);
                }
            }
            src.setStatus(SectionLifecycleStatus.MERGED);
            src.setActive(false);
            src.setMergedIntoSectionId(target.getId());
            src.setCanceledAt(LocalDateTime.now());
            if (request.getReason() != null && !request.getReason().isBlank()) {
                src.setCanceledReason(request.getReason());
            }
            src.setCurrentStudents(0);
            sectionRepository.save(src);
        }

        int newCurrent = (int) enrollmentRepository.findBySectionId(target.getId()).stream()
                .filter(e -> e.getStatus() == vn.com.nws.cms.modules.academic.domain.enums.EnrollmentStatus.ENROLLED)
                .count();
        target.setCurrentStudents(newCurrent);
        sectionRepository.save(target);

        return toResponse(target);
    }

    @Override
    @Transactional
    public void deleteSection(Long id) {
        sectionTimeSlotRepository.replaceSectionTimeSlots(id, List.of());
        enrollmentRepository.findBySectionId(id).forEach(e -> enrollmentRepository.deleteById(e.getId()));
        sectionRepository.deleteById(id);
    }

    @Override
    public List<SectionTimeSlotResponse> getSectionTimeSlots(Long sectionId) {
        return sectionTimeSlotRepository.findBySectionId(sectionId).stream()
                .map(SectionTimeSlotResponse::fromDomain)
                .toList();
    }

    @Override
    @Transactional
    public void replaceSectionTimeSlots(Long sectionId, List<SectionTimeSlotRequest> slots) {
        List<SectionTimeSlot> domainSlots = slots == null ? List.of() : slots.stream()
                .map(s -> SectionTimeSlot.builder()
                        .sectionId(sectionId)
                        .dayOfWeek(s.getDayOfWeek())
                        .startTime(s.getStartTime())
                        .endTime(s.getEndTime())
                        .build())
                .toList();
        sectionTimeSlotRepository.replaceSectionTimeSlots(sectionId, domainSlots);
    }

    private void validateEnrollmentWindow(LocalDate start, LocalDate end) {
        if (start == null || end == null) return;
        if (end.isBefore(start)) {
            throw new BusinessException("Ngày kết thúc đăng ký phải sau hoặc bằng ngày bắt đầu");
        }
    }

    private boolean sameSchedule(List<SectionTimeSlot> a, List<SectionTimeSlot> b) {
        if (a == null) a = List.of();
        if (b == null) b = List.of();
        if (a.size() != b.size()) return false;
        for (int i = 0; i < a.size(); i++) {
            SectionTimeSlot x = a.get(i);
            SectionTimeSlot y = b.get(i);
            if (x.getDayOfWeek() != y.getDayOfWeek()) return false;
            if (!x.getStartTime().equals(y.getStartTime())) return false;
            if (!x.getEndTime().equals(y.getEndTime())) return false;
        }
        return true;
    }

    private SectionResponse toResponse(Section s) {
        List<SectionTimeSlotResponse> slots = sectionTimeSlotRepository.findBySectionId(s.getId()).stream()
                .map(SectionTimeSlotResponse::fromDomain)
                .toList();

        SubjectResponse subject = s.getSubject() == null ? null : SubjectResponse.builder()
                .id(s.getSubject().getId())
                .name(s.getSubject().getName())
                .code(s.getSubject().getCode())
                .credit(s.getSubject().getCredits())
                .description(s.getSubject().getDescription())
                .active(s.getSubject().isActive())
                .processWeight(s.getSubject().getProcessWeight())
                .examWeight(s.getSubject().getExamWeight())
                .createdAt(s.getSubject().getCreatedAt())
                .updatedAt(s.getSubject().getUpdatedAt())
                .build();

        SemesterResponse semester = s.getSemester() == null ? null : SemesterResponse.builder()
                .id(s.getSemester().getId())
                .code(s.getSemester().getCode())
                .name(s.getSemester().getName())
                .startDate(s.getSemester().getStartDate())
                .endDate(s.getSemester().getEndDate())
                .active(s.getSemester().isActive())
                .secondaryActive(s.getSemester().isSecondaryActive())
                .createdAt(s.getSemester().getCreatedAt())
                .updatedAt(s.getSemester().getUpdatedAt())
                .build();

        UserResponse teacher = s.getTeacher() == null ? null : UserResponse.builder()
                .id(s.getTeacher().getId())
                .username(s.getTeacher().getUsername())
                .email(s.getTeacher().getEmail())
                .build();

        return SectionResponse.builder()
                .id(s.getId())
                .name(s.getName())
                .code(s.getCode())
                .maxStudents(s.getMaxStudents())
                .currentStudents(s.getCurrentStudents())
                .active(s.isActive())
                .status(s.getStatus())
                .minStudents(s.getMinStudents())
                .canceledAt(s.getCanceledAt())
                .canceledReason(s.getCanceledReason())
                .mergedIntoSectionId(s.getMergedIntoSectionId())
                .subject(subject)
                .semester(semester)
                .teacher(teacher)
                .enrollmentStartDate(s.getEnrollmentStartDate())
                .enrollmentEndDate(s.getEnrollmentEndDate())
                .registrationEnabled(s.isRegistrationEnabled())
                .timeSlots(slots)
                .createdAt(s.getCreatedAt())
                .updatedAt(s.getUpdatedAt())
                .build();
    }
}
