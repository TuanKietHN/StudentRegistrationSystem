package vn.com.nws.cms.modules.academic.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.com.nws.cms.modules.academic.infrastructure.persistence.entity.ProgramSubjectEntity;

import java.util.List;

@Repository
public interface JpaProgramSubjectRepository extends JpaRepository<ProgramSubjectEntity, Long> {
    List<ProgramSubjectEntity> findByAcademicProgramId(Long programId);
    boolean existsByAcademicProgramIdAndSubjectId(Long programId, Long subjectId);
    void deleteByAcademicProgramId(Long programId);

    @Modifying
    @Query(value = """
            INSERT INTO program_subjects (program_id, subject_id, semester, subject_type, pass_score, created_at, updated_at, is_deleted)
            VALUES (:programId, :subjectId, :semester, :subjectType, :passScore, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, false)
            ON CONFLICT (program_id, subject_id) DO UPDATE SET
                semester = EXCLUDED.semester,
                subject_type = EXCLUDED.subject_type,
                pass_score = EXCLUDED.pass_score,
                updated_at = CURRENT_TIMESTAMP,
                is_deleted = false
            """, nativeQuery = true)
    void upsertNative(@Param("programId") Long programId,
                      @Param("subjectId") Long subjectId,
                      @Param("semester") Integer semester,
                      @Param("subjectType") String subjectType,
                      @Param("passScore") Double passScore);
}
