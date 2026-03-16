package vn.com.nws.cms.modules.academic.infrastructure.persistence.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;
import vn.com.nws.cms.modules.academic.domain.model.Subject;
import vn.com.nws.cms.modules.academic.domain.repository.SubjectRepository;
import vn.com.nws.cms.modules.academic.infrastructure.persistence.entity.SubjectEntity;
import vn.com.nws.cms.modules.academic.infrastructure.persistence.mapper.SubjectMapper;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class SubjectRepositoryImpl implements SubjectRepository {

    private final JpaSubjectRepository jpaRepository;
    private final SubjectMapper mapper;

    @Override
    public Subject save(Subject subject) {
        SubjectEntity entity = mapper.toEntity(subject);
        if (subject.getId() != null) {
            entity.setId(subject.getId());
        }
        return mapper.toDomain(jpaRepository.save(entity));
    }

    @Override
    public Optional<Subject> findById(Long id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<Subject> findByCode(String code) {
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
    public List<Subject> search(String keyword, Boolean active, int page, int size) {
        String normalizedKeyword = keyword == null || keyword.isBlank() ? null : keyword;
        if (normalizedKeyword == null && active == null) {
            return jpaRepository.findAll(PageRequest.of(page - 1, size)).getContent().stream()
                    .map(mapper::toDomain)
                    .collect(Collectors.toList());
        }
        if (normalizedKeyword == null) {
            return jpaRepository.findAllByActive(active, PageRequest.of(page - 1, size)).getContent().stream()
                    .map(mapper::toDomain)
                    .collect(Collectors.toList());
        }
        return jpaRepository.search(normalizedKeyword, active, PageRequest.of(page - 1, size)).getContent().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public long count(String keyword, Boolean active) {
        String normalizedKeyword = keyword == null || keyword.isBlank() ? null : keyword;
        if (normalizedKeyword == null && active == null) {
            return jpaRepository.count();
        }
        if (normalizedKeyword == null) {
            return jpaRepository.countByActive(active);
        }
        return jpaRepository.count(normalizedKeyword, active);
    }
}

