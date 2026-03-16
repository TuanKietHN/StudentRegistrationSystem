package vn.com.nws.cms.modules.academic.api.dto;

import lombok.Data;

@Data
public class CohortUpdateRequest {
    private String name;
    private String code;
    private Integer startYear;
    private Integer endYear;
    private Boolean active;
}
