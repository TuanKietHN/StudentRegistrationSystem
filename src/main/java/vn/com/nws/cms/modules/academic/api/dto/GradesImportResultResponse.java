package vn.com.nws.cms.modules.academic.api.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class GradesImportResultResponse {
    private int totalRows;
    private int imported;
    private int skippedLocked;
    private int skippedNotFound;
    private int skippedInvalid;
    private List<String> errors;
}

