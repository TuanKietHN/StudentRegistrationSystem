package vn.com.nws.cms.modules.academic.domain.repository;

import vn.com.nws.cms.modules.academic.domain.enums.CohortLifecycleStatus;
import vn.com.nws.cms.modules.academic.domain.model.Cohort;

import java.util.List;
import java.util.Optional;

public interface CohortRepository {
    Cohort save(Cohort cohort);
    Optional<Cohort> findById(Long id);
    Optional<Cohort> findByCode(String code);
    void deleteById(Long id);
    boolean existsByCode(String code);

    List<Cohort> search(String keyword, Long semesterId, Long classId, Long teacherId, Boolean active, CohortLifecycleStatus status, int page, int size);
    long count(String keyword, Long semesterId, Long classId, Long teacherId, Boolean active, CohortLifecycleStatus status);
}

