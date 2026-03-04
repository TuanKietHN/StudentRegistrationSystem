package vn.com.nws.cms.modules.academic.application.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.com.nws.cms.common.dto.PageResponse;
import vn.com.nws.cms.common.exception.BusinessException;
import vn.com.nws.cms.modules.academic.api.dto.*;
import vn.com.nws.cms.modules.academic.application.ClassService;
import vn.com.nws.cms.modules.academic.domain.model.CourseClass;
import vn.com.nws.cms.modules.academic.domain.repository.ClassRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClassServiceImpl implements ClassService {

    private final ClassRepository classRepository;

    @Override
    public PageResponse<ClassResponse> getClasses(ClassFilterRequest request) {
        List<CourseClass> classes = classRepository.search(request.getKeyword(), request.getActive(), request.getPage(), request.getSize());
        long totalElements = classRepository.count(request.getKeyword(), request.getActive());
        int totalPages = (int) Math.ceil((double) totalElements / request.getSize());

        List<ClassResponse> responses = classes.stream().map(this::toResponse).collect(Collectors.toList());

        return PageResponse.<ClassResponse>builder()
                .page(request.getPage())
                .size(request.getSize())
                .totalElements(totalElements)
                .totalPages(totalPages)
                .data(responses)
                .build();
    }

    @Override
    public ClassResponse getClassById(Long id) {
        CourseClass clazz = classRepository.findById(id).orElseThrow(() -> new BusinessException("Class not found"));
        return toResponse(clazz);
    }

    @Override
    @Transactional
    public ClassResponse createClass(ClassCreateRequest request) {
        if (classRepository.existsByCode(request.getCode())) {
            throw new BusinessException("Class code already exists");
        }

        Short processWeight = request.getProcessWeight() == null ? 40 : request.getProcessWeight();
        Short examWeight = request.getExamWeight() == null ? 60 : request.getExamWeight();
        if ((processWeight + examWeight) != 100) {
            throw new BusinessException("Tỷ lệ điểm không hợp lệ (process + exam phải = 100)");
        }

        CourseClass clazz = CourseClass.builder()
                .name(request.getName())
                .code(request.getCode())
                .credits(request.getCredit())
                .description(request.getDescription())
                .active(request.isActive())
                .processWeight(processWeight)
                .examWeight(examWeight)
                .build();

        clazz = classRepository.save(clazz);
        return toResponse(clazz);
    }

    @Override
    @Transactional
    public ClassResponse updateClass(Long id, ClassUpdateRequest request) {
        CourseClass clazz = classRepository.findById(id).orElseThrow(() -> new BusinessException("Class not found"));

        if (request.getCode() != null && !request.getCode().equals(clazz.getCode())) {
            if (classRepository.existsByCode(request.getCode())) {
                throw new BusinessException("Class code already exists");
            }
            clazz.setCode(request.getCode());
        }

        if (request.getName() != null) clazz.setName(request.getName());
        if (request.getCredit() != null) clazz.setCredits(request.getCredit());
        if (request.getDescription() != null) clazz.setDescription(request.getDescription());
        if (request.getActive() != null) clazz.setActive(request.getActive());
        if (request.getProcessWeight() != null) clazz.setProcessWeight(request.getProcessWeight());
        if (request.getExamWeight() != null) clazz.setExamWeight(request.getExamWeight());

        if (clazz.getProcessWeight() != null && clazz.getExamWeight() != null && (clazz.getProcessWeight() + clazz.getExamWeight()) != 100) {
            throw new BusinessException("Tỷ lệ điểm không hợp lệ (process + exam phải = 100)");
        }

        clazz = classRepository.save(clazz);
        return toResponse(clazz);
    }

    @Override
    @Transactional
    public void deleteClass(Long id) {
        if (classRepository.findById(id).isEmpty()) {
            throw new BusinessException("Class not found");
        }
        classRepository.deleteById(id);
    }

    private ClassResponse toResponse(CourseClass clazz) {
        return ClassResponse.builder()
                .id(clazz.getId())
                .name(clazz.getName())
                .code(clazz.getCode())
                .credit(clazz.getCredits())
                .description(clazz.getDescription())
                .active(clazz.isActive())
                .processWeight(clazz.getProcessWeight())
                .examWeight(clazz.getExamWeight())
                .createdAt(clazz.getCreatedAt())
                .updatedAt(clazz.getUpdatedAt())
                .build();
    }
}
