package vn.com.nws.cms.modules.academic.application.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.com.nws.cms.common.dto.PageResponse;
import vn.com.nws.cms.common.exception.BusinessException;
import vn.com.nws.cms.modules.academic.api.dto.*;
import vn.com.nws.cms.modules.academic.application.CohortService;
import vn.com.nws.cms.modules.academic.domain.model.Cohort;
import vn.com.nws.cms.modules.academic.domain.repository.CohortRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CohortServiceImpl implements CohortService {

    private final CohortRepository cohortRepository;

    @Override
    public PageResponse<CohortResponse> getCohorts(CohortFilterRequest request) {
        List<Cohort> cohorts = cohortRepository.search(
                request.getKeyword(),
                request.getStartYear(),
                request.getEndYear(),
                request.getActive(),
                request.getPage(),
                request.getSize()
        );

        long totalElements = cohortRepository.count(
                request.getKeyword(),
                request.getStartYear(),
                request.getEndYear(),
                request.getActive()
        );

        int totalPages = (int) Math.ceil((double) totalElements / request.getSize());

        List<CohortResponse> responses = cohorts.stream().map(this::toResponse).collect(Collectors.toList());

        return PageResponse.<CohortResponse>builder()
                .page(request.getPage())
                .size(request.getSize())
                .totalElements(totalElements)
                .totalPages(totalPages)
                .data(responses)
                .build();
    }

    @Override
    public CohortResponse getCohortById(Long id) {
        Cohort cohort = cohortRepository.findById(id).orElseThrow(() -> new BusinessException("Cohort not found"));
        return toResponse(cohort);
    }

    @Override
    @Transactional
    public CohortResponse createCohort(CohortCreateRequest request) {
        if (cohortRepository.existsByCode(request.getCode())) {
            throw new BusinessException("Cohort code already exists");
        }

        Cohort cohort = Cohort.builder()
                .name(request.getName())
                .code(request.getCode())
                .startYear(request.getStartYear())
                .endYear(request.getEndYear())
                .active(request.isActive())
                .build();

        cohort = cohortRepository.save(cohort);
        return toResponse(cohort);
    }

    @Override
    @Transactional
    public CohortResponse updateCohort(Long id, CohortUpdateRequest request) {
        Cohort cohort = cohortRepository.findById(id).orElseThrow(() -> new BusinessException("Cohort not found"));

        if (request.getCode() != null && !request.getCode().equals(cohort.getCode())) {
            if (cohortRepository.existsByCode(request.getCode())) {
                throw new BusinessException("Cohort code already exists");
            }
            cohort.setCode(request.getCode());
        }
        if (request.getName() != null) cohort.setName(request.getName());
        if (request.getStartYear() != null) cohort.setStartYear(request.getStartYear());
        if (request.getEndYear() != null) cohort.setEndYear(request.getEndYear());
        if (request.getActive() != null) cohort.setActive(request.getActive());

        cohort = cohortRepository.save(cohort);
        return toResponse(cohort);
    }

    @Override
    @Transactional
    public void deleteCohort(Long id) {
        if (cohortRepository.findById(id).isEmpty()) {
            throw new BusinessException("Cohort not found");
        }
        cohortRepository.deleteById(id);
    }

    private CohortResponse toResponse(Cohort c) {
        return CohortResponse.builder()
                .id(c.getId())
                .name(c.getName())
                .code(c.getCode())
                .active(c.isActive())
                .startYear(c.getStartYear())
                .endYear(c.getEndYear())
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                .build();
    }
}

