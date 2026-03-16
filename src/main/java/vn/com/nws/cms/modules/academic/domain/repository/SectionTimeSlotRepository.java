package vn.com.nws.cms.modules.academic.domain.repository;

import vn.com.nws.cms.modules.academic.domain.model.SectionTimeSlot;

import java.time.LocalTime;
import java.util.List;

public interface SectionTimeSlotRepository {
    List<SectionTimeSlot> findBySectionId(Long sectionId);
    void replaceSectionTimeSlots(Long sectionId, List<SectionTimeSlot> slots);

    boolean existsStudentScheduleConflict(Long studentId, Long semesterId, Long targetSectionId, short dayOfWeek, LocalTime startTime, LocalTime endTime);
}

