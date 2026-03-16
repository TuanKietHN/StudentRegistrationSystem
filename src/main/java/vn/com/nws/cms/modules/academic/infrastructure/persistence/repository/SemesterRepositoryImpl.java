package vn.com.nws.cms.modules.academic.infrastructure.persistence.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;
import vn.com.nws.cms.modules.academic.domain.model.Semester;
import vn.com.nws.cms.modules.academic.domain.repository.SemesterRepository;
import vn.com.nws.cms.modules.academic.infrastructure.persistence.entity.SemesterEntity;
import vn.com.nws.cms.modules.academic.infrastructure.persistence.mapper.SemesterMapper;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class SemesterRepositoryImpl implements SemesterRepository {

    private final JpaSemesterRepository jpaSemesterRepository;
    private final SemesterMapper semesterMapper;

    @Override
    public Semester save(Semester semester) {
        SemesterEntity entity = semesterMapper.toEntity(semester);
        if (semester.getId() != null) {
            entity.setId(semester.getId());
        }
        SemesterEntity savedEntity = jpaSemesterRepository.save(entity);
        return semesterMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Semester> findById(Long id) {
        return jpaSemesterRepository.findById(id).map(semesterMapper::toDomain);
    }

    @Override
    public Optional<Semester> findByCode(String code) {
        return jpaSemesterRepository.findByCode(code).map(semesterMapper::toDomain);
    }

    @Override
    public void deleteById(Long id) {
        jpaSemesterRepository.deleteById(id);
    }

    @Override
    public boolean existsByCode(String code) {
        return jpaSemesterRepository.existsByCode(code);
    }

    @Override
    public List<Semester> search(String keyword, Boolean active, int page, int size) {
        String normalizedKeyword = keyword == null || keyword.isBlank() ? null : keyword;
        if (normalizedKeyword == null && active == null) {
            return jpaSemesterRepository.findAll(PageRequest.of(page - 1, size)).getContent().stream()
                    .map(semesterMapper::toDomain)
                    .collect(Collectors.toList());
        }
        if (normalizedKeyword == null) {
            return jpaSemesterRepository.findAllByActive(active, PageRequest.of(page - 1, size)).getContent().stream()
                    .map(semesterMapper::toDomain)
                    .collect(Collectors.toList());
        }
        return jpaSemesterRepository.search(normalizedKeyword, active, PageRequest.of(page - 1, size))
                .getContent().stream()
                .map(semesterMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public long count(String keyword, Boolean active) {
        String normalizedKeyword = keyword == null || keyword.isBlank() ? null : keyword;
        if (normalizedKeyword == null && active == null) {
            return jpaSemesterRepository.count();
        }
        if (normalizedKeyword == null) {
            return jpaSemesterRepository.countByActive(active);
        }
        return jpaSemesterRepository.count(normalizedKeyword, active);
    }

    @Override
    public List<Semester> searchSecondaryActive(String keyword, int page, int size) {
        String normalizedKeyword = keyword == null || keyword.isBlank() ? null : keyword;
        return jpaSemesterRepository.searchSecondaryActive(normalizedKeyword, PageRequest.of(page - 1, size))
                .getContent().stream()
                .map(semesterMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Semester> findActiveSemester() {
        LocalDate now = LocalDate.now();
        return jpaSemesterRepository
                .findFirstByActiveTrueAndStartDateLessThanEqualAndEndDateGreaterThanEqualOrderByStartDateDesc(now, now)
                .or(() -> jpaSemesterRepository.findFirstByActiveTrueOrderByStartDateDesc())
                .map(semesterMapper::toDomain);
    }

    @Override
    public Optional<Semester> findSecondaryActiveSemester() {
        return jpaSemesterRepository.findFirstBySecondaryActiveTrueOrderByStartDateDesc().map(semesterMapper::toDomain);
    }
}
