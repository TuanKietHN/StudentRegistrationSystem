package vn.com.nws.cms.modules.academic.domain.repository;

import vn.com.nws.cms.modules.academic.domain.model.AcademicProgram;

import java.util.List;
import java.util.Optional;

public interface AcademicProgramRepository {
    AcademicProgram save(AcademicProgram program);
    Optional<AcademicProgram> findById(Long id);
    Optional<AcademicProgram> findByCode(String code);
    List<AcademicProgram> findAll();
    void deleteById(Long id);
    boolean existsByCode(String code);
}
