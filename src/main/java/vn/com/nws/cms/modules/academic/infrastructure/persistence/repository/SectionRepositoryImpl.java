package vn.com.nws.cms.modules.academic.infrastructure.persistence.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import vn.com.nws.cms.common.exception.BusinessException;
import vn.com.nws.cms.modules.academic.domain.enums.SectionLifecycleStatus;
import vn.com.nws.cms.modules.academic.domain.model.Section;
import vn.com.nws.cms.modules.academic.domain.repository.SectionRepository;
import vn.com.nws.cms.modules.academic.infrastructure.persistence.entity.SectionEntity;
import vn.com.nws.cms.modules.academic.infrastructure.persistence.entity.TeacherEntity;
import vn.com.nws.cms.modules.academic.infrastructure.persistence.mapper.SectionMapper;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class SectionRepositoryImpl implements SectionRepository {

    private final JpaSectionRepository jpaRepository;
    private final JpaTeacherRepository jpaTeacherRepository;
    private final SectionMapper mapper;

    @Override
    @Transactional
    public Section save(Section section) {
        SectionEntity entity = mapper.toEntity(section);
        if (section.getId() != null) {
            entity.setId(section.getId());
        }
        if (section.getTeacher() != null && section.getTeacher().getId() != null) {
            Long teacherUserId = section.getTeacher().getId();
            Long teacherProfileId = jpaTeacherRepository.findByUserId(teacherUserId)
                    .map(t -> t.getId())
                    .orElseThrow(() -> new BusinessException("Teacher profile not found"));
            TeacherEntity teacherEntity = jpaTeacherRepository.findById(teacherProfileId).orElse(null);
            entity.setTeacher(teacherEntity);
        } else {
            entity.setTeacher(null);
        }
        return mapper.toDomain(jpaRepository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Section> findById(Long id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Section> findByCode(String code) {
        return jpaRepository.findByCode(code).map(mapper::toDomain);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByCode(String code) {
        return jpaRepository.existsByCode(code);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Section> search(String keyword, Long semesterId, Long subjectId, Long teacherId, Boolean active, SectionLifecycleStatus status, int page, int size) {
        String normalizedKeyword = keyword == null ? "" : keyword.trim();
        if (normalizedKeyword.isBlank() && semesterId == null && subjectId == null && teacherId == null && active == null && status == null) {
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
        return jpaRepository.search(normalizedKeyword, semesterId, subjectId, teacherProfileId, active, status, PageRequest.of(page - 1, size))
                .getContent().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public long count(String keyword, Long semesterId, Long subjectId, Long teacherId, Boolean active, SectionLifecycleStatus status) {
        String normalizedKeyword = keyword == null ? "" : keyword.trim();
        if (normalizedKeyword.isBlank() && semesterId == null && subjectId == null && teacherId == null && active == null && status == null) {
            return jpaRepository.count();
        }
        Long teacherProfileId = null;
        if (teacherId != null) {
            teacherProfileId = jpaTeacherRepository.findByUserId(teacherId).map(t -> t.getId()).orElse(null);
            if (teacherProfileId == null) {
                return 0;
            }
        }
        return jpaRepository.count(normalizedKeyword, semesterId, subjectId, teacherProfileId, active, status);
    }
}
