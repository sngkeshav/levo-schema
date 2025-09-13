package com.levo.schema.controller;

import com.levo.schema.response.MessageResponse;
import com.levo.schema.response.SchemaResponse;
import com.levo.schema.response.ValidationResponse;
import com.levo.schema.service.SchemaService;
import com.levo.schema.util.Constants;
import com.levo.schema.util.SchemaValidator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.Map;

/**
 * REST Controller for Schema management
 * Provides endpoints that mimic CLI operations:
 * - POST /schemas/upload -> levo import
 * - GET /schemas/latest/{app} -> levo test
 */
@Slf4j
@RestController
@RequestMapping(Constants.API_VERSION + Constants.SCHEMAS_PATH)
@RequiredArgsConstructor
@Tag(name = "Schemas", description = "OpenAPI Schema management APIs")
public class SchemaController {

    private final SchemaService schemaService;

    @Operation(
            summary = "Upload OpenAPI schema file",
            description = "Upload OpenAPI schema file (JSON/YAML) for an application or service. " +
                    "Mimics CLI command: levo import --spec file --application app [--service service]"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Schema uploaded successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid schema or validation failed"),
            @ApiResponse(responseCode = "413", description = "File too large")
    })
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SchemaResponse> uploadSchemaFile(
            @Parameter(description = "OpenAPI schema file (JSON or YAML)")
            @RequestPart("file") MultipartFile file,

            @Parameter(description = "Application name", required = true)
            @RequestPart("application") @NotBlank String application,

            @Parameter(description = "Service name (optional)")
            @RequestPart(value = "service", required = false) String service) {

        log.info("Received schema upload request for application: {}, service: {}, file: {}",
                application, service, file.getOriginalFilename());

        SchemaResponse response = schemaService.uploadSchemaFromFile(file, application, service);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(
            summary = "Upload OpenAPI schema as JSON",
            description = "Upload OpenAPI schema as JSON payload for an application or service"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Schema uploaded successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid schema or validation failed")
    })
    @PostMapping(value = "/upload-json", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<SchemaResponse> uploadSchemaJson(
            @Parameter(description = "Application name", required = true)
            @RequestParam("application") @NotBlank String application,

            @Parameter(description = "Service name (optional)")
            @RequestParam(value = "service", required = false) String service,

            @Parameter(description = "OpenAPI schema as JSON object")
            @RequestBody Map<String, Object> schemaContent) {

        log.info("Received JSON schema upload request for application: {}, service: {}", application, service);

        // Convert JSON map to string for processing
        String jsonContent;
        try {
            jsonContent = new com.fasterxml.jackson.databind.ObjectMapper()
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsString(schemaContent);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize JSON content", e);
        }

        SchemaResponse response = schemaService.uploadSchemaContent(jsonContent, application, service);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(
            summary = "Get latest schema",
            description = "Get the latest schema version for an application or service. " +
                    "Mimics CLI command: levo test --application app [--service service]"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Latest schema retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "No schema found for the specified application/service")
    })
    @GetMapping("/latest/{applicationName}")
    public ResponseEntity<SchemaResponse> getLatestSchema(
            @Parameter(description = "Application name")
            @PathVariable @NotBlank String applicationName,

            @Parameter(description = "Service name (optional)")
            @RequestParam(value = "service", required = false) String serviceName) {

        log.debug("Received request for latest schema - application: {}, service: {}",
                applicationName, serviceName);

        SchemaResponse response = schemaService.getLatestSchema(applicationName, serviceName);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Get schema by ID",
            description = "Get schema details by schema ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Schema retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Schema not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<SchemaResponse> getSchemaById(
            @Parameter(description = "Schema ID")
            @PathVariable @Positive Long id) {

        log.debug("Received request for schema with ID: {}", id);

        SchemaResponse response = schemaService.getSchemaById(id);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Get schema with content by ID",
            description = "Get schema with full content by schema ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Schema with content retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Schema not found")
    })
    @GetMapping("/{id}/content")
    public ResponseEntity<SchemaResponse> getSchemaWithContentById(
            @Parameter(description = "Schema ID")
            @PathVariable @Positive Long id) {

        log.debug("Received request for schema content with ID: {}", id);

        SchemaResponse response = schemaService.getSchemaWithContentById(id);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "List schemas for application",
            description = "List all schemas for an application or service with pagination"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Schemas retrieved successfully")
    })
    @GetMapping("/{applicationName}/list")
    public ResponseEntity<Page<SchemaResponse>> getSchemasForApplication(
            @Parameter(description = "Application name")
            @PathVariable @NotBlank String applicationName,

            @Parameter(description = "Service name (optional)")
            @RequestParam(value = "service", required = false) String serviceName,

            @Parameter(description = "Pagination parameters")
            Pageable pageable) {

        log.debug("Received request to list schemas - application: {}, service: {}, pagination: {}",
                applicationName, serviceName, pageable);

        Page<SchemaResponse> response = schemaService.getSchemasForApplication(applicationName, serviceName, pageable);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Delete schema",
            description = "Delete a specific schema by ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Schema deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Schema not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse> deleteSchema(
            @Parameter(description = "Schema ID")
            @PathVariable @Positive Long id) {

        log.info("Received request to delete schema with ID: {}", id);

        schemaService.deleteSchema(id);
        MessageResponse response = MessageResponse.success("Schema deleted successfully");
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Validate schema without uploading",
            description = "Validate an OpenAPI schema without storing it"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Schema is valid"),
            @ApiResponse(responseCode = "400", description = "Schema validation failed")
    })
    @PostMapping(value = "/validate", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ValidationResponse> validateSchemaFile(
            @Parameter(description = "OpenAPI schema file (JSON or YAML)")
            @RequestPart("file") MultipartFile file) {

        log.info("Received schema validation request for file: {}", file.getOriginalFilename());

        try {
            String content = new String(file.getBytes());
            boolean isValid = schemaService.validateSchemaOnly(content);

            if (isValid) {
                ValidationResponse response = ValidationResponse.builder()
                        .valid(true)
                        .message("Schema is valid")
                        .build();
                return ResponseEntity.ok(response);
            } else {
                ValidationResponse response = ValidationResponse.builder()
                        .valid(false)
                        .message("Schema validation failed")
                        .build();
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            log.error("Error during schema validation", e);
            ValidationResponse response = ValidationResponse.builder()
                    .valid(false)
                    .message("Schema validation error: " + e.getMessage())
                    .build();
            return ResponseEntity.badRequest().body(response);
        }
    }

    @Operation(
            summary = "Validate schema content as JSON",
            description = "Validate OpenAPI schema content as JSON without storing it"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Schema is valid"),
            @ApiResponse(responseCode = "400", description = "Schema validation failed")
    })
    @PostMapping(value = "/validate-json", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ValidationResponse> validateSchemaJson(
            @Parameter(description = "OpenAPI schema as JSON object")
            @RequestBody Map<String, Object> schemaContent) {

        log.info("Received JSON schema validation request");

        try {
            // Convert JSON map to string for processing
            String jsonContent = new com.fasterxml.jackson.databind.ObjectMapper()
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsString(schemaContent);

            SchemaValidator.ValidationResult validationResult = schemaService.validateSchemaContent(jsonContent);

            ValidationResponse response = ValidationResponse.builder()
                    .valid(validationResult.isValid())
                    .message(validationResult.getMessage())
                    .warnings(validationResult.getWarnings())
                    .build();

            if (validationResult.isValid()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }

        } catch (Exception e) {
            log.error("Error during JSON schema validation", e);
            ValidationResponse response = ValidationResponse.builder()
                    .valid(false)
                    .message("Schema validation error: " + e.getMessage())
                    .build();
            return ResponseEntity.badRequest().body(response);
        }
    }

    @Operation(
            summary = "Health check for schema operations",
            description = "Check if schema upload and retrieval operations are working"
    )
    @GetMapping("/health")
    public ResponseEntity<MessageResponse> healthCheck() {
        log.debug("Schema service health check requested");

        MessageResponse response = MessageResponse.success("Schema service is healthy");
        return ResponseEntity.ok(response);
    }
}