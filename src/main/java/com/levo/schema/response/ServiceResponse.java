package com.levo.schema.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceResponse {
    private Long id;
    private String name;
    private String description;
    private Long applicationId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
