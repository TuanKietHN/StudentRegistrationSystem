package vn.com.nws.cms.modules.academic.infrastructure.persistence.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import vn.com.nws.cms.modules.academic.domain.model.Department;
import vn.com.nws.cms.modules.academic.domain.model.Student;
import vn.com.nws.cms.modules.academic.domain.repository.StudentRepository;
import vn.com.nws.cms.modules.academic.infrastructure.persistence.entity.AdminClassEntity;
import vn.com.nws.cms.modules.academic.infrastructure.persistence.entity.DepartmentEntity;
import vn.com.nws.cms.modules.academic.infrastructure.persistence.entity.StudentEntity;
import vn.com.nws.cms.modules.academic.infrastructure.persistence.mapper.DepartmentMapper;
import vn.com.nws.cms.modules.academic.infrastructure.persistence.mapper.StudentMapper;
import vn.com.nws.cms.modules.auth.infrastructure.persistence.entity.UserEntity;
import vn.com.nws.cms.modules.auth.infrastructure.persistence.repository.JpaUserRepository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class StudentRepositoryImpl implements StudentRepository {

    private final StudentJpaRepository jpaRepository;
    private final JpaUserRepository jpaUserRepository;
    private final DepartmentJpaRepository departmentJpaRepository;
    private final JpaAdminClassRepository adminClassJpaRepository;
    private final StudentMapper studentMapper;
    private final DepartmentMapper departmentMapper;

    @Override
    public Student save(Student student) {
        StudentEntity entity;
        if (student.getId() != null) {
            Optional<StudentEntity> existingOpt = jpaRepository.findById(student.getId());
            if (existingOpt.isPresent()) {
                StudentEntity existing = existingOpt.get();
                StudentEntity mapped = studentMapper.toEntity(student);
                existing.setStudentCode(mapped.getStudentCode());
                existing.setPhone(mapped.getPhone());
                existing.setActive(mapped.isActive());
                entity = existing;
            } else {
                entity = studentMapper.toEntity(student);
                entity.setId(student.getId());
            }
        } else {
            entity = studentMapper.toEntity(student);
        }

        if (student.getUser() != null && student.getUser().getId() != null) {
            UserEntity userEntity = jpaUserRepository.findById(student.getUser().getId()).orElse(null);
            if (userEntity != null) {
                entity.setUser(userEntity);
            }
        }

        if (student.getDepartment() != null && student.getDepartment().getId() != null) {
            DepartmentEntity dept = departmentJpaRepository.findById(student.getDepartment().getId()).orElse(null);
            entity.setDepartment(dept);
        } else {
            entity.setDepartment(null);
        }

        if (student.getAdminClass() != null && student.getAdminClass().getId() != null) {
            AdminClassEntity adminClass = adminClassJpaRepository.findById(student.getAdminClass().getId()).orElse(null);
            entity.setAdminClass(adminClass);
        } else {
            entity.setAdminClass(null);
        }

        StudentEntity saved = jpaRepository.save(entity);
        return mapToDomain(saved);
    }

    @Override
    public Optional<Student> findById(Long id) {
        return jpaRepository.findById(id).map(this::mapToDomain);
    }

    @Override
    public Optional<Student> findByUserId(Long userId) {
        return jpaRepository.findByUserId(userId).map(this::mapToDomain);
    }

    @Override
    public boolean existsByStudentCode(String studentCode) {
        return jpaRepository.existsByStudentCode(studentCode);
    }

    @Override
    public boolean existsByStudentCodeAndIdNot(String studentCode, Long id) {
        return jpaRepository.existsByStudentCodeAndIdNot(studentCode, id);
    }

    @Override
    public Page<Student> findAll(String keyword, Long departmentId, Boolean active, Pageable pageable) {
        return jpaRepository.findAll(keyword, departmentId, active, pageable).map(this::mapToDomain);
    }

    @Override
    public List<Student> findByAdminClassId(Long adminClassId) {
        return jpaRepository.findByAdminClassId(adminClassId).stream().map(this::mapToDomain).toList();
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }

    private Student mapToDomain(StudentEntity entity) {
        Student student = studentMapper.toDomain(entity);
        if (entity.getDepartment() != null) {
            Department department = departmentMapper.toDomain(entity.getDepartment());
            student.setDepartment(department);
        }
        return student;
    }
}
