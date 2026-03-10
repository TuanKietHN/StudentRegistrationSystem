package vn.com.nws.cms.common.domain.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Audit {
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
