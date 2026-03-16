package vn.com.nws.cms.modules.academic.infrastructure.persistence.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;
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
    private final CohortMapper mapper;

    @Override
    public Cohort save(Cohort cohort) {
        CohortEntity entity = mapper.toEntity(cohort);
        if (cohort.getId() != null) {
            entity.setId(cohort.getId());
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
    public List<Cohort> search(String keyword, Integer startYear, Integer endYear, Boolean active, int page, int size) {
        String normalizedKeyword = keyword == null || keyword.isBlank() ? null : keyword;
        if (normalizedKeyword == null && startYear == null && endYear == null && active == null) {
            return jpaRepository.findAll(PageRequest.of(page - 1, size)).getContent().stream()
                    .map(mapper::toDomain)
                    .collect(Collectors.toList());
        }
        if (normalizedKeyword == null) {
            return jpaRepository.searchWithoutKeyword(startYear, endYear, active, PageRequest.of(page - 1, size)).getContent().stream()
                    .map(mapper::toDomain)
                    .collect(Collectors.toList());
        }
        return jpaRepository.search(normalizedKeyword, startYear, endYear, active, PageRequest.of(page - 1, size)).getContent().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public long count(String keyword, Integer startYear, Integer endYear, Boolean active) {
        String normalizedKeyword = keyword == null || keyword.isBlank() ? null : keyword;
        if (normalizedKeyword == null && startYear == null && endYear == null && active == null) {
            return jpaRepository.count();
        }
        if (normalizedKeyword == null) {
            return jpaRepository.countWithoutKeyword(startYear, endYear, active);
        }
        return jpaRepository.count(normalizedKeyword, startYear, endYear, active);
    }
}
