package vn.com.nws.cms.modules.academic.infrastructure.persistence.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;
import vn.com.nws.cms.common.exception.BusinessException;
import vn.com.nws.cms.modules.academic.domain.enums.CohortLifecycleStatus;
import vn.com.nws.cms.modules.academic.domain.model.Cohort;
import vn.com.nws.cms.modules.academic.domain.repository.CohortRepository;
import vn.com.nws.cms.modules.academic.infrastructure.persistence.entity.CohortEntity;
import vn.com.nws.cms.modules.academic.infrastructure.persistence.mapper.CohortMapper;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class CohortRepositoryImpl implements CohortRepository {

    private final JpaCohortRepository jpaRepository;
    private final JpaTeacherRepository jpaTeacherRepository;
    private final CohortMapper mapper;

    @Override
    public Cohort save(Cohort cohort) {
        CohortEntity entity = mapper.toEntity(cohort);
        if (cohort.getId() != null) {
            entity.setId(cohort.getId());
        }
        if (cohort.getTeacher() != null && cohort.getTeacher().getId() != null) {
            Long teacherUserId = cohort.getTeacher().getId();
            Long teacherProfileId = jpaTeacherRepository.findByUserId(teacherUserId)
                    .map(t -> t.getId())
                    .orElseThrow(() -> new BusinessException("Teacher profile not found"));
            entity.setTeacherId(teacherProfileId);
        } else {
            entity.setTeacherId(null);
        }
        return mapper.toDomain(jpaRepository.save(entity));
    }

    @Override
    public Optional<Cohort> findById(Long id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<Cohort> findByCode(String code) {
        return jpaRepository.findByCode(code).map(mapper::toDomain);
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public boolean existsByCode(String code) {
        return jpaRepository.existsByCode(code);
    }

    @Override
    public List<Cohort> search(String keyword, Long semesterId, Long classId, Long teacherId, Boolean active, CohortLifecycleStatus status, int page, int size) {
        String normalizedKeyword = keyword == null || keyword.isBlank() ? null : keyword;
        if (normalizedKeyword == null && semesterId == null && classId == null && teacherId == null && active == null && status == null) {
            return jpaRepository.findAll(PageRequest.of(page - 1, size)).getContent().stream()
                    .map(mapper::toDomain)
                    .collect(Collectors.toList());
        }
        Long teacherProfileId = null;
        if (teacherId != null) {
            teacherProfileId = jpaTeacherRepository.findByUserId(teacherId).map(t -> t.getId()).orElse(null);
            if (teacherProfileId == null) {
                return List.of();
            }
        }
        if (normalizedKeyword == null) {
            return jpaRepository.search(null, semesterId, classId, teacherProfileId, active, status, PageRequest.of(page - 1, size))
                    .getContent().stream()
                    .map(mapper::toDomain)
                    .collect(Collectors.toList());
        }
        return jpaRepository.search(normalizedKeyword, semesterId, classId, teacherProfileId, active, status, PageRequest.of(page - 1, size)).getContent().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public long count(String keyword, Long semesterId, Long classId, Long teacherId, Boolean active, CohortLifecycleStatus status) {
        String normalizedKeyword = keyword == null || keyword.isBlank() ? null : keyword;
        if (normalizedKeyword == null && semesterId == null && classId == null && teacherId == null && active == null && status == null) {
            return jpaRepository.count();
        }
        Long teacherProfileId = null;
        if (teacherId != null) {
            teacherProfileId = jpaTeacherRepository.findByUserId(teacherId).map(t -> t.getId()).orElse(null);
            if (teacherProfileId == null) {
                return 0;
            }
        }
        return jpaRepository.count(normalizedKeyword, semesterId, classId, teacherProfileId, active, status);
    }
}

