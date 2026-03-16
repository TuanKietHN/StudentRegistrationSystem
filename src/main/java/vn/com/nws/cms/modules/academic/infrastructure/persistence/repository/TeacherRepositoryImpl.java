package vn.com.nws.cms.modules.academic.infrastructure.persistence.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import vn.com.nws.cms.common.exception.BusinessException;
import vn.com.nws.cms.modules.academic.domain.model.Department;
import vn.com.nws.cms.modules.academic.domain.model.Teacher;
import vn.com.nws.cms.modules.academic.domain.repository.TeacherRepository;
import vn.com.nws.cms.modules.academic.infrastructure.persistence.entity.DepartmentEntity;
import vn.com.nws.cms.modules.academic.infrastructure.persistence.entity.TeacherEntity;
import vn.com.nws.cms.modules.academic.infrastructure.persistence.mapper.DepartmentMapper;
import vn.com.nws.cms.modules.academic.infrastructure.persistence.mapper.TeacherMapper;
import vn.com.nws.cms.modules.auth.infrastructure.persistence.entity.UserEntity;
import vn.com.nws.cms.modules.auth.infrastructure.persistence.repository.JpaUserRepository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class TeacherRepositoryImpl implements TeacherRepository {

    private final TeacherJpaRepository teacherJpaRepository;
    private final JpaUserRepository jpaUserRepository; // For finding User entity
    private final DepartmentJpaRepository departmentJpaRepository;
    private final TeacherMapper teacherMapper;
    private final DepartmentMapper departmentMapper;

    @Override
    public Teacher save(Teacher teacher) {
        TeacherEntity entity = teacherMapper.toEntity(teacher);
        if (teacher.getId() != null) {
            entity.setId(teacher.getId());
        }

        // Set User
        if (teacher.getUser() != null && teacher.getUser().getId() != null) {
            UserEntity userEntity = jpaUserRepository.findById(teacher.getUser().getId())
                    .orElseThrow(() -> new BusinessException("User not found"));
            entity.setUser(userEntity);
        }

        // Set Department
        if (teacher.getDepartment() != null && teacher.getDepartment().getId() != null) {
            DepartmentEntity departmentEntity = departmentJpaRepository.findById(teacher.getDepartment().getId())
                    .orElseThrow(() -> new BusinessException("Department not found"));
            entity.setDepartment(departmentEntity);
        } else {
            entity.setDepartment(null);
        }

        TeacherEntity savedEntity = teacherJpaRepository.save(entity);
        return mapToDomain(savedEntity);
    }

    @Override
    public Optional<Teacher> findById(Long id) {
        return teacherJpaRepository.findById(id).map(this::mapToDomain);
    }

    @Override
    public Optional<Teacher> findByUserId(Long userId) {
        return teacherJpaRepository.findByUserId(userId).map(this::mapToDomain);
    }

    @Override
    public Optional<Teacher> findByEmployeeCode(String employeeCode) {
        return teacherJpaRepository.findByEmployeeCode(employeeCode).map(this::mapToDomain);
    }

    @Override
    public void deleteById(Long id) {
        teacherJpaRepository.deleteById(id);
    }

    @Override
    public boolean existsByEmployeeCode(String employeeCode) {
        return teacherJpaRepository.existsByEmployeeCode(employeeCode);
    }

    @Override
    public boolean existsByEmployeeCodeAndIdNot(String employeeCode, Long id) {
        return teacherJpaRepository.existsByEmployeeCodeAndIdNot(employeeCode, id);
    }

    @Override
    public Page<Teacher> findAll(String keyword, Long departmentId, Boolean active, Pageable pageable) {
        String normalizedKeyword = keyword == null || keyword.isBlank() ? null : keyword;
        if (normalizedKeyword == null && departmentId == null && active == null) {
            return teacherJpaRepository.findAll(pageable).map(this::mapToDomain);
        }
        return teacherJpaRepository.findAll(normalizedKeyword, departmentId, active, pageable).map(this::mapToDomain);
    }

    private Teacher mapToDomain(TeacherEntity entity) {
        Teacher teacher = teacherMapper.toDomain(entity);
        if (entity.getDepartment() != null) {
            // Map department explicitly to avoid cycle if we rely on Mapper's default behavior (which ignores it)
            // But since DepartmentMapper maps HeadTeacher (Teacher), we get:
            // Teacher -> Department -> HeadTeacher (with null Department)
            Department dept = departmentMapper.toDomain(entity.getDepartment());
            teacher.setDepartment(dept);
        }
        return teacher;
    }
}
