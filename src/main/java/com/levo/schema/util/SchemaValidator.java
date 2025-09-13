package com.levo.schema.util;

import io.micrometer.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Map;


@Slf4j
@Component
public class SchemaValidator {

    /**
     * Validate OpenAPI schema structure and content
     *
     * @param content Schema content as Map
     * @param strictMode Whether to use strict validation
     * @return ValidationResult containing validation status and messages
     */
    public static ValidationResult validateSchema(Map<String, Object> content, boolean strictMode) {
        if (content == null || content.isEmpty()) {
            return ValidationResult.failure("Schema content cannot be null or empty");
        }

        try {
            ValidationResult basicValidation = validateBasicStructure(content);
            if (basicValidation.isValid()) {
                return basicValidation;
            }
            ValidationResult businessValidation = validateBusinessRules(content);
            if (businessValidation.isValid()) {
                return businessValidation;
            }

            return ValidationResult.success("Schema validation passed");

        } catch (Exception e) {
            log.error("Unexpected error during schema validation", e);
            return ValidationResult.failure("Validation failed due to unexpected error: " + e.getMessage());
        }
    }

    /**
     * Validate basic OpenAPI structure
     *
     * @param content Schema content
     * @return ValidationResult
     */
    private static ValidationResult validateBasicStructure(Map<String, Object> content) {
        if (!content.containsKey(Constants.OPENAPI_FIELD) && !content.containsKey(Constants.SWAGGER_FIELD)) {
            return ValidationResult.failure(
                    "Schema must contain either 'openapi' (OpenAPI 3.x) or 'swagger' (OpenAPI 2.x) field"
            );
        }

        if (!content.containsKey(Constants.INFO_FIELD)) {
            return ValidationResult.failure("Schema must contain 'info' field");
        }

        if (!content.containsKey(Constants.PATHS_FIELD)) {
            return ValidationResult.failure("Schema must contain 'paths' field");
        }

        Object version = content.get(Constants.OPENAPI_FIELD);
        if (version != null) {
            String versionStr = version.toString();
            if (!isValidOpenApiVersion(versionStr)) {
                return ValidationResult.failure(
                        "Invalid OpenAPI version format: " + versionStr +
                                ". Expected format: 3.x.x (e.g., 3.0.0, 3.0.1, 3.1.0)"
                );
            }
        }

        Object infoObj = content.get(Constants.INFO_FIELD);
        if (!(infoObj instanceof Map)) {
            return ValidationResult.failure("'info' field must be an object");
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> info = (Map<String, Object>) infoObj;
        if (!info.containsKey("title")) {
            return ValidationResult.failure("'info' object must contain 'title' field");
        }

        if (!info.containsKey("version")) {
            return ValidationResult.failure("'info' object must contain 'version' field");
        }

        return ValidationResult.success("Basic structure validation passed");
    }

    /**
     *
     * @param content Schema content
     * @param strictMode Whether to use strict validation
     * @return ValidationResult
     */

    /**
     *
     * @param content Schema content
     * @return ValidationResult
     */
    private static ValidationResult validateBusinessRules(Map<String, Object> content) {
        // Add custom business rules here

        // Rule 1: Check if paths object is not empty
        Object pathsObj = content.get(Constants.PATHS_FIELD);
        if (pathsObj instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> paths = (Map<String, Object>) pathsObj;
            if (paths.isEmpty()) {
                return ValidationResult.failure("Schema must contain at least one API path");
            }
        }

        return ValidationResult.success("Business rules validation passed");
    }

    /**
     * Check if OpenAPI version is valid
     *
     * @param version Version string
     * @return true if valid
     */
    private static boolean isValidOpenApiVersion(String version) {
        if (StringUtils.isBlank(version)) {
            return false;
        }

        // Support OpenAPI 3.x.x format
        return version.matches("^3\\.\\d+\\.\\d+$");
    }

    /**
     * Validation result class
     */
    public static class ValidationResult {
        private final boolean valid;
        private final String message;
        private final List<String> warnings;

        private ValidationResult(boolean valid, String message, List<String> warnings) {
            this.valid = valid;
            this.message = message;
            this.warnings = warnings;
        }

        public static ValidationResult success(String message) {
            return new ValidationResult(true, message, null);
        }

        public static ValidationResult success(String message, List<String> warnings) {
            return new ValidationResult(true, message, warnings);
        }

        public static ValidationResult failure(String message) {
            return new ValidationResult(false, message, null);
        }

        public boolean isValid() {return valid;}

        public String getMessage() {
            return message;
        }

        public List<String> getWarnings() {
            return warnings;
        }

        public boolean hasWarnings() {
            return warnings != null && !warnings.isEmpty();
        }
    }
}