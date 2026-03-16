package vn.com.nws.cms.modules.academic.domain.repository;

import vn.com.nws.cms.modules.academic.domain.model.CohortTimeSlot;

import java.time.LocalTime;
import java.util.List;

public interface CohortTimeSlotRepository {
    List<CohortTimeSlot> findByCohortId(Long cohortId);
    void replaceCohortTimeSlots(Long cohortId, List<CohortTimeSlot> slots);

    boolean existsStudentScheduleConflict(Long studentId, Long semesterId, Long targetCohortId, short dayOfWeek, LocalTime startTime, LocalTime endTime);
}

