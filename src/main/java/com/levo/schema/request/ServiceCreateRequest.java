package com.levo.schema.request;

import com.levo.schema.util.Constants;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceCreateRequest {

    @NotBlank(message = "Service name is required")
    @Size(max = Constants.MAX_NAME_LENGTH, message = "Service name must not exceed " + Constants.MAX_NAME_LENGTH + " characters")
    private String name;

    @Size(max = Constants.MAX_DESCRIPTION_LENGTH, message = "Description must not exceed " + Constants.MAX_DESCRIPTION_LENGTH + " characters")
    private String description;

    @NotNull(message = "Application ID is required")
    @Positive(message = "Application ID must be positive")
    private Long applicationId;
}

