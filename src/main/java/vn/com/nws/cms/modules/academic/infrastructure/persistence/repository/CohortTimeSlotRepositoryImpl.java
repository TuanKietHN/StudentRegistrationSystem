package vn.com.nws.cms.modules.academic.infrastructure.persistence.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import vn.com.nws.cms.modules.academic.domain.model.CohortTimeSlot;
import vn.com.nws.cms.modules.academic.domain.repository.CohortTimeSlotRepository;
import vn.com.nws.cms.modules.academic.infrastructure.persistence.entity.CohortEntity;
import vn.com.nws.cms.modules.academic.infrastructure.persistence.entity.CohortTimeSlotEntity;
import vn.com.nws.cms.modules.academic.infrastructure.persistence.mapper.CohortTimeSlotMapper;

import java.time.LocalTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class CohortTimeSlotRepositoryImpl implements CohortTimeSlotRepository {

    private final CohortTimeSlotJpaRepository jpaRepository;
    private final JpaCohortRepository cohortJpaRepository;
    private final CohortTimeSlotMapper mapper;

    @Override
    public List<CohortTimeSlot> findByCohortId(Long cohortId) {
        return jpaRepository.findByCohortId(cohortId).stream().map(mapper::toDomain).toList();
    }

    @Override
    @Transactional
    public void replaceCohortTimeSlots(Long cohortId, List<CohortTimeSlot> slots) {
        jpaRepository.deleteByCohortId(cohortId);
        if (slots == null || slots.isEmpty()) {
            return;
        }
        CohortEntity cohort = cohortJpaRepository.findById(cohortId).orElse(null);
        if (cohort == null) {
            return;
        }
        for (CohortTimeSlot slot : slots) {
            CohortTimeSlotEntity entity = mapper.toEntity(slot);
            entity.setCohort(cohort);
            jpaRepository.save(entity);
        }
    }

    @Override
    public boolean existsStudentScheduleConflict(Long studentId, Long semesterId, Long targetCohortId, short dayOfWeek, LocalTime startTime, LocalTime endTime) {
        return jpaRepository.existsStudentScheduleConflict(studentId, semesterId, targetCohortId, dayOfWeek, startTime, endTime);
    }
}

