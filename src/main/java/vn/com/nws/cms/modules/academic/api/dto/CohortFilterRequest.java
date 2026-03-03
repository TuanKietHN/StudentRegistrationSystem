package vn.com.nws.cms.modules.academic.api.dto;

import lombok.Data;

@Data
public class CohortFilterRequest {
    private String keyword;
    private Integer startYear;
    private Integer endYear;
    private Boolean active;
    private int page = 1;
    private int size = 10;
}
