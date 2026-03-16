package vn.com.nws.cms.modules.academic.api.dto;

import lombok.Data;

@Data
public class ClassFilterRequest {
    private Integer page = 1;
    private Integer size = 10;
    private String keyword;
    private Boolean active;
}

