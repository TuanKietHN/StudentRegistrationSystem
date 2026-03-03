package vn.com.nws.cms.modules.academic.infrastructure.persistence.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import vn.com.nws.cms.modules.academic.domain.model.SectionTimeSlot;
import vn.com.nws.cms.modules.academic.domain.repository.SectionTimeSlotRepository;
import vn.com.nws.cms.modules.academic.infrastructure.persistence.entity.SectionEntity;
import vn.com.nws.cms.modules.academic.infrastructure.persistence.entity.SectionTimeSlotEntity;
import vn.com.nws.cms.modules.academic.infrastructure.persistence.mapper.SectionTimeSlotMapper;

import java.time.LocalTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class SectionTimeSlotRepositoryImpl implements SectionTimeSlotRepository {

    private final SectionTimeSlotJpaRepository jpaRepository;
    private final JpaSectionRepository sectionJpaRepository;
    private final SectionTimeSlotMapper mapper;

    @Override
    public List<SectionTimeSlot> findBySectionId(Long sectionId) {
        return jpaRepository.findBySectionId(sectionId).stream().map(mapper::toDomain).toList();
    }

    @Override
    @Transactional
    public void replaceSectionTimeSlots(Long sectionId, List<SectionTimeSlot> slots) {
        jpaRepository.deleteBySectionId(sectionId);
        if (slots == null || slots.isEmpty()) {
            return;
        }
        SectionEntity section = sectionJpaRepository.findById(sectionId).orElse(null);
        if (section == null) {
            return;
        }
        for (SectionTimeSlot slot : slots) {
            SectionTimeSlotEntity entity = mapper.toEntity(slot);
            entity.setSection(section);
            jpaRepository.save(entity);
        }
    }

    @Override
    public boolean existsStudentScheduleConflict(Long studentId, Long semesterId, Long targetSectionId, short dayOfWeek, LocalTime startTime, LocalTime endTime) {
        return jpaRepository.existsStudentScheduleConflict(studentId, semesterId, targetSectionId, dayOfWeek, startTime, endTime);
    }
}

