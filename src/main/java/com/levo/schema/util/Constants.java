package com.levo.schema.util;

public final class Constants {

    private Constants() {
        // Utility class
    }

    // API Paths
    public static final String API_VERSION = "/v1";
    public static final String APPLICATIONS_PATH = "/applications";
    public static final String SERVICES_PATH = "/services";
    public static final String SCHEMAS_PATH = "/schemas";

    // File Storage
    public static final String DEFAULT_STORAGE_PATH = "./schema_storage";
    public static final String SCHEMA_FILE_PREFIX = "v";

    // File Formats
    public static final String JSON_FORMAT = "json";
    public static final String YAML_FORMAT = "yaml";
    public static final String YML_FORMAT = "yml";

    // OpenAPI Fields
    public static final String OPENAPI_FIELD = "openapi";
    public static final String INFO_FIELD = "info";
    public static final String PATHS_FIELD = "paths";
    public static final String SWAGGER_FIELD = "swagger";

    // HTTP Headers
    public static final String CONTENT_TYPE_JSON = "application/json";
    public static final String CONTENT_TYPE_YAML = "application/yaml";

    // Validation
    public static final int MAX_NAME_LENGTH = 255;
    public static final int MAX_DESCRIPTION_LENGTH = 1000;
    public static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    // Error Messages
    public static final String APPLICATION_NOT_FOUND = "Application not found: %s";
    public static final String SERVICE_NOT_FOUND = "Service not found: %s";
    public static final String SCHEMA_NOT_FOUND = "Schema not found for %s version %d";
    public static final String DUPLICATE_APPLICATION = "Application already exists: %s";
    public static final String DUPLICATE_SERVICE = "Service already exists: %s in application: %s";
    public static final String INVALID_SCHEMA_FORMAT = "Invalid schema format. Must be valid JSON or YAML";
    public static final String INVALID_OPENAPI_SCHEMA = "Invalid OpenAPI schema: %s";
    public static final String FILE_STORAGE_ERROR = "Error storing schema file: %s";
    public static final String FILE_READ_ERROR = "Error reading schema file: %s";

    // Success Messages
    public static final String SCHEMA_UPLOADED_SUCCESS = "Schema uploaded successfully for %s version %d";
    public static final String APPLICATION_CREATED_SUCCESS = "Application created successfully: %s";
    public static final String SERVICE_CREATED_SUCCESS = "Service created successfully: %s";
}