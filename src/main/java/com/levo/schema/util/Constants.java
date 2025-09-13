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

    // OpenAPI Fields
    public static final String OPENAPI_FIELD = "openapi";
    public static final String INFO_FIELD = "info";
    public static final String PATHS_FIELD = "paths";
    public static final String SWAGGER_FIELD = "swagger";

    // Validation
    public static final int MAX_NAME_LENGTH = 255;
    public static final int MAX_DESCRIPTION_LENGTH = 1000;

    // Error Messages
    public static final String APPLICATION_NOT_FOUND = "Application not found: %s";
    public static final String SERVICE_NOT_FOUND = "Service not found: %s";
    public static final String DUPLICATE_APPLICATION = "Application already exists: %s";
    public static final String INVALID_OPENAPI_SCHEMA = "Invalid OpenAPI schema: %s";
}