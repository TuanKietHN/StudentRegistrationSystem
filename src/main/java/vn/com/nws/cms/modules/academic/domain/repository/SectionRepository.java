package vn.com.nws.cms.modules.academic.domain.repository;

import vn.com.nws.cms.modules.academic.domain.enums.SectionLifecycleStatus;
import vn.com.nws.cms.modules.academic.domain.model.Section;

import java.util.List;
import java.util.Optional;

public interface SectionRepository {
    Section save(Section section);
    Optional<Section> findById(Long id);
    Optional<Section> findByCode(String code);
    void deleteById(Long id);
    boolean existsByCode(String code);

    List<Section> search(String keyword, Long semesterId, Long subjectId, Long teacherId, Boolean active, SectionLifecycleStatus status, int page, int size);
    long count(String keyword, Long semesterId, Long subjectId, Long teacherId, Boolean active, SectionLifecycleStatus status);
}

