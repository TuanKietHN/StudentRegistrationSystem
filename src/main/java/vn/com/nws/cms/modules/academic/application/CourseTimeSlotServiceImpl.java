package vn.com.nws.cms.modules.academic.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.com.nws.cms.common.exception.BusinessException;
import vn.com.nws.cms.modules.academic.api.dto.CourseTimeSlotRequest;
import vn.com.nws.cms.modules.academic.api.dto.CourseTimeSlotResponse;
import vn.com.nws.cms.modules.academic.domain.model.CourseTimeSlot;
import vn.com.nws.cms.modules.academic.domain.repository.CourseTimeSlotRepository;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseTimeSlotServiceImpl implements CourseTimeSlotService {

    private final CourseTimeSlotRepository repository;

    @Override
    public List<CourseTimeSlotResponse> getCourseTimeSlots(Long courseId) {
        return repository.findByCourseId(courseId).stream().map(CourseTimeSlotResponse::fromDomain).toList();
    }

    @Override
    @Transactional
    public List<CourseTimeSlotResponse> replaceCourseTimeSlots(Long courseId, List<CourseTimeSlotRequest> requests) {
        List<CourseTimeSlot> slots = new ArrayList<>();
        if (requests != null) {
            for (CourseTimeSlotRequest r : requests) {
                if (r.getStartTime() != null && r.getEndTime() != null && !r.getStartTime().isBefore(r.getEndTime())) {
                    throw new BusinessException("Thời gian học không hợp lệ");
                }
                slots.add(CourseTimeSlot.builder()
                        .courseId(courseId)
                        .dayOfWeek(r.getDayOfWeek())
                        .startTime(r.getStartTime())
                        .endTime(r.getEndTime())
                        .build());
            }
        }

        validateNoOverlap(slots);

        repository.replaceCourseTimeSlots(courseId, slots);
        return repository.findByCourseId(courseId).stream().map(CourseTimeSlotResponse::fromDomain).toList();
    }

    private void validateNoOverlap(List<CourseTimeSlot> slots) {
        Map<Short, List<CourseTimeSlot>> byDay = slots.stream().collect(Collectors.groupingBy(CourseTimeSlot::getDayOfWeek));
        for (Map.Entry<Short, List<CourseTimeSlot>> entry : byDay.entrySet()) {
            List<CourseTimeSlot> list = entry.getValue().stream()
                    .sorted(Comparator.comparing(CourseTimeSlot::getStartTime))
                    .toList();
            for (int i = 1; i < list.size(); i++) {
                LocalTime prevEnd = list.get(i - 1).getEndTime();
                LocalTime currStart = list.get(i).getStartTime();
                if (currStart.isBefore(prevEnd)) {
                    throw new BusinessException("Lịch học bị trùng trong cùng một ngày");
                }
            }
        }
    }
}

