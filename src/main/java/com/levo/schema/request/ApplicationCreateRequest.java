package com.levo.schema.request;

import com.levo.schema.util.Constants;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationCreateRequest {

    @NotBlank(message = "Application name is required")
    @Size(max = Constants.MAX_NAME_LENGTH, message = "Application name must not exceed " + Constants.MAX_NAME_LENGTH + " characters")
    private String name;

    @Size(max = Constants.MAX_DESCRIPTION_LENGTH, message = "Description must not exceed " + Constants.MAX_DESCRIPTION_LENGTH + " characters")
    private String description;
}
