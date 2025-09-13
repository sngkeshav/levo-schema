package com.levo.schema.response;

import com.levo.schema.entity.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SchemaResponse {
    private Long id;
    private Long applicationId;
    private String applicationName;
    private Long serviceId;
    private String serviceName;
    private Integer version;
    private String filePath;
    private Schema.FileFormat fileFormat;
    private Boolean isLatest;
    private String content;
    private LocalDateTime createdAt;
    private String scopeIdentifier;
}
