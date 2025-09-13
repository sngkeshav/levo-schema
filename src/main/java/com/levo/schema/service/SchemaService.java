package com.levo.schema.service;

import com.levo.schema.request.SchemaWithContentWrapper;
import com.levo.schema.response.ApplicationResponse;
import com.levo.schema.response.SchemaResponse;
import com.levo.schema.mapper.SchemaMapper;
import com.levo.schema.entity.Application;
import com.levo.schema.entity.Schema;
import com.levo.schema.entity.Service;
import com.levo.schema.exception.ResourceNotFoundException;
import com.levo.schema.exception.SchemaValidationException;
import com.levo.schema.repository.SchemaRepository;
import com.levo.schema.response.ServiceResponse;
import com.levo.schema.util.Constants;
import com.levo.schema.util.FileUtils;
import com.levo.schema.util.SchemaValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service class for managing Schema entities and operations
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SchemaService {

    private final SchemaRepository schemaRepository;
    private final SchemaMapper schemaMapper;
    private final ApplicationService applicationService;
    private final ServiceService serviceService;
    private final FileStorageService fileStorageService;

    @Value("${levo.schema.validation.strict-mode:true}")
    private boolean strictValidationMode;

    /**
     * Upload schema from multipart file
     *
     * @param file Multipart file containing schema
     * @param applicationName Application name
     * @param serviceName Service name (optional)
     * @return Schema with content response
     */
    @Transactional
    public SchemaResponse uploadSchemaFromFile(MultipartFile file, String applicationName, String serviceName) {
        try {
            log.info("Uploading schema file for application: {}, service: {}", applicationName, serviceName);

            // Read file content
            String content = new String(file.getBytes());
            return uploadSchemaContent(content, applicationName, serviceName);

        } catch (Exception e) {
            log.error("Failed to upload schema from file", e);
            throw new SchemaValidationException("Failed to upload schema from file: " + e.getMessage(), e);
        }
    }

    /**
     * Upload schema from string content
     *
     * @param content Schema content as string
     * @param applicationName Application name
     * @param serviceName Service name (optional)
     * @return Schema with content response
     */
    @Transactional
    public SchemaResponse uploadSchemaContent(String content, String applicationName, String serviceName) {
        log.info("Uploading schema content for application: {}, service: {}", applicationName, serviceName);

        try {
            Schema.FileFormat format = FileUtils.detectFileFormat(content);
            Map<String, Object> parsedContent = FileUtils.parseContent(content);

            SchemaValidator.ValidationResult validationResult =
                    SchemaValidator.validateSchema(parsedContent, strictValidationMode);

            if (!validationResult.isValid()) {
                throw new SchemaValidationException(
                        String.format(Constants.INVALID_OPENAPI_SCHEMA, validationResult.getMessage()));
            }

            if (validationResult.hasWarnings()) {
                log.warn("Schema validation warnings: {}", validationResult.getWarnings());
            }

            Application application = applicationService.getOrCreateApplicationEntity(applicationName);

            Service service = null;
            if (StringUtils.isNotBlank(serviceName)) {
                service = serviceService.getOrCreateServiceEntity(serviceName, application);
            }

            Integer nextVersion = getNextVersionNumber(application.getId(),
                    service != null ? service.getId() : null);

            markPreviousSchemasAsNotLatest(application.getId(), service != null ? service.getId() : null);

            String filePath = fileStorageService.saveSchema(content, applicationName, serviceName, nextVersion, format);

            Schema.SchemaBuilder schemaBuilder = Schema.builder()
                    .applicationId(application.getId())
                    .version(nextVersion)
                    .fileFormat(format)
                    .filePath(filePath)
                    .isLatest(true)
                    .content(content)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now());

            if (service != null) {
                schemaBuilder.serviceId(service.getId());
            }

            Schema schema = schemaBuilder.build();

            Schema savedSchema = schemaRepository.save(schema);
            log.info("Schema uploaded successfully with ID: {} and version: {}", savedSchema.getId(), savedSchema.getVersion());

            return schemaMapper.toSchemaWithContentResponse(
                new SchemaWithContentWrapper(
                    savedSchema, content
                )
            );

        } catch (SchemaValidationException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to upload schema content", e);
            throw new SchemaValidationException("Failed to upload schema content: " + e.getMessage(), e);
        }
    }

    /**
     * Get schema by ID
     *
     * @param id Schema ID
     * @return Schema response
     */
    public SchemaResponse getSchemaById(Long id) {
        log.info("Fetching schema with ID: {}", id);

        Schema schema = schemaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Schema not found with ID: " + id));

        return schemaMapper.toSchemaResponse(schema);
    }

    /**
     * Get schema with content by ID
     *
     * @param id Schema ID
     * @return Schema with content response
     */
    public SchemaResponse getSchemaWithContentById(Long id) {
        log.info("Fetching schema with content for ID: {}", id);

        Schema schema = schemaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Schema not found with ID: " + id));

        String content = fileStorageService.getSchemaFileContent(schema.getFilePath());

        return schemaMapper.toSchemaWithContentResponse(
            new SchemaWithContentWrapper(
                    schema, content
            )
        );
    }

    /**
     * Get the latest schema for application and service
     *
     * @param applicationName Application name
     * @param serviceName Service name (optional)
     * @return Schema with content response
     */
    public SchemaResponse getLatestSchema(String applicationName, String serviceName) {
        log.info("Fetching latest schema for application: {}, service: {}", applicationName, serviceName);

        ApplicationResponse application = applicationService.getApplicationByName(applicationName);
        ServiceResponse service = null;

        if (StringUtils.isNotBlank(serviceName)) {
            service = serviceService.getServiceByNameAndApplication(serviceName, applicationName);
        }

        Schema schema = findLatestSchema(application.getId(), service != null ? service.getId() : null)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("No schema found for application: %s, service: %s", applicationName, serviceName)));

        String content = fileStorageService.getSchemaFileContent(schema.getFilePath());

        return schemaMapper.toSchemaWithContentResponse(
            new SchemaWithContentWrapper(
                    schema, content
            )
        );

    }

    /**
     * Get all schemas for application with pagination
     *
     * @param applicationName Application name
     * @param serviceName Service name (optional)
     * @param pageable Pagination information
     * @return Page of schema responses
     */
    public Page<SchemaResponse> getSchemasForApplication(String applicationName, String serviceName, Pageable pageable) {
        log.info("Fetching schemas for application: {}, service: {}", applicationName, serviceName);

        ApplicationResponse application = applicationService.getApplicationByName(applicationName);
        ServiceResponse service = null;

        if (StringUtils.isNotBlank(serviceName)) {
            service = serviceService.getServiceByNameAndApplication(serviceName, applicationName);
        }

        Page<Schema> schemas;
        if (service != null) {
            schemas = schemaRepository.findByApplicationIdAndServiceIdOrderByVersionDesc(
                    application.getId(), service.getId(), pageable);
        } else {
            schemas = schemaRepository.findByApplicationIdAndServiceIdIsNullOrderByVersionDesc(
                    application.getId(), pageable);
        }

        return schemas.map(schemaMapper::toSchemaResponse);
    }

    /**
     *
     * @param id Schema ID
     */
    @Transactional
    public void deleteSchema(Long id) {
        log.info("Deleting schema with ID: {}", id);

        Schema schema = schemaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Schema not found with ID: " + id));

        // Delete file from storage
        fileStorageService.deleteSchema(schema.getFilePath());

        // Delete schema entity
        schemaRepository.delete(schema);

        // If this was the latest schema, mark the previous version as latest
        if (schema.getIsLatest()) {
            updateLatestSchemaAfterDeletion(schema.getApplication().getId(),
                    schema.getService() != null ? schema.getService().getId() : null);
        }

        log.info("Schema deleted successfully with ID: {}", id);
    }

    /**
     * Get next version number for schema
     *
     * @param applicationId Application ID
     * @param serviceId Service ID (optional)
     * @return Next version number
     */
    private Integer getNextVersionNumber(Long applicationId, Long serviceId) {
        if (serviceId != null) {
            return schemaRepository.getNextVersionForService(applicationId, serviceId);
        } else {
            return schemaRepository.getNextVersionForApplication(applicationId);
        }
    }

    /**
     * Mark previous schemas as not latest
     *
     * @param applicationId Application ID
     * @param serviceId Service ID (optional)
     */
    private void markPreviousSchemasAsNotLatest(Long applicationId, Long serviceId) {
        if (serviceId != null) {
            schemaRepository.markAllAsNotLatestForService(applicationId, serviceId);
        } else {
            schemaRepository.markAllAsNotLatestForApplication(applicationId);
        }
    }

    /**
     * Find latest schema
     *
     * @param applicationId Application ID
     * @param serviceId Service ID (optional)
     * @return Optional schema
     */
    private Optional<Schema> findLatestSchema(Long applicationId, Long serviceId) {
        if (serviceId != null) {
            return schemaRepository.findByApplicationIdAndServiceIdAndIsLatestTrue(applicationId, serviceId);
        } else {
            return schemaRepository.findByApplicationIdAndServiceIdIsNullAndIsLatestTrue(applicationId);
        }
    }

    /**
     * Update latest schema after deletion
     *
     * @param applicationId Application ID
     * @param serviceId Service ID (optional)
     */
    private void updateLatestSchemaAfterDeletion(Long applicationId, Long serviceId) {
        Optional<Schema> latestSchema;

        if (serviceId != null) {
            latestSchema = schemaRepository.findFirstByApplicationIdAndServiceIdOrderByVersionDesc(applicationId, serviceId);
        } else {
            latestSchema = schemaRepository.findFirstByApplicationIdAndServiceIdIsNullOrderByVersionDesc(applicationId);
        }

        if (latestSchema.isPresent()) {
            Schema schema = latestSchema.get();
            schema.setIsLatest(true);
            schemaRepository.save(schema);
            log.info("Marked schema version {} as latest after deletion", schema.getVersion());
        }
    }

    /**
     * Get schema by version for application and service
     *
     * @param applicationName Application name
     * @param serviceName Service name (optional)
     * @param version Version number
     * @return Schema with content response
     */
    public SchemaResponse getSchemaByVersion(String applicationName, String serviceName, Integer version) {
        log.info("Fetching schema version {} for application: {}, service: {}", version, applicationName, serviceName);

        ApplicationResponse application = applicationService.getApplicationByName(applicationName);
        ServiceResponse service = null;

        if (StringUtils.isNotBlank(serviceName)) {
            service = serviceService.getServiceByNameAndApplication(serviceName, applicationName);
        }

        Optional<Schema> schemaOpt;
        if (service != null) {
            schemaOpt = schemaRepository.findByApplicationIdAndServiceIdAndVersion(application.getId(), service.getId(), version);
        } else {
            schemaOpt = schemaRepository.findByApplicationIdAndServiceIdIsNullAndVersion(application.getId(), version);
        }

        Schema schema = schemaOpt.orElseThrow(() -> new ResourceNotFoundException(
                String.format("Schema version %d not found for application: %s, service: %s", version, applicationName, serviceName)));

        String content = fileStorageService.getSchemaFileContent(schema.getFilePath());

        return schemaMapper.toSchemaWithContentResponse(
            new SchemaWithContentWrapper(
                    schema, content
            )
        );
    }

    /**
     * List all schema versions for application and service
     *
     * @param applicationName Application name
     * @param serviceName Service name (optional)
     * @return List of schema responses
     */
    public List<SchemaResponse> listSchemaVersions(String applicationName, String serviceName) {
        log.info("Listing schema versions for application: {}, service: {}", applicationName, serviceName);

        List<Schema> schemas;
        if (StringUtils.isNotBlank(serviceName)) {
            schemas = schemaRepository.findByApplicationNameAndServiceName(applicationName, serviceName);
        } else {
            schemas = schemaRepository.findByApplicationName(applicationName);
        }

        return schemas.stream()
                .map(schemaMapper::toSchemaResponse)
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * List all schemas with pagination
     *
     * @param pageable Pagination information
     * @return Page of schema responses
     */
    public Page<SchemaResponse> listSchemas(Pageable pageable) {
        log.info("Listing all schemas with pagination: {}", pageable);

        Page<Schema> schemas = schemaRepository.findAll(pageable);
        return schemas.map(schemaMapper::toSchemaResponse);
    }

    /**
     * Get schema statistics
     *
     * @param applicationName Application name
     * @param serviceName Service name (optional)
     * @return Schema statistics
     */
    public SchemaStatistics getSchemaStatistics(String applicationName, String serviceName) {
        log.info("Getting schema statistics for application: {}, service: {}", applicationName, serviceName);

        ApplicationResponse application = applicationService.getApplicationByName(applicationName);
        ServiceResponse service = null;

        if (StringUtils.isNotBlank(serviceName)) {
            service = serviceService.getServiceByNameAndApplication(serviceName, applicationName);
        }

        long totalSchemas;
        if (service != null) {
            totalSchemas = schemaRepository.countByServiceId(service.getId());
        } else {
            totalSchemas = schemaRepository.countByApplicationId(application.getId());
        }

        return SchemaStatistics.builder()
                .applicationName(applicationName)
                .serviceName(serviceName)
                .totalVersions(totalSchemas)
                .build();
    }

    /**
     * Validate schema content only (without saving)
     *
     * @param content Schema content
     * @return true if valid, false otherwise
     */
    public boolean validateSchemaOnly(String content) {
        try {
            SchemaValidator.ValidationResult result = validateSchemaContent(content);
            return result.isValid();
        } catch (Exception e) {
            log.error("Error during schema validation", e);
            return false;
        }
    }

    /**
     * Schema statistics data class
     */
    @lombok.Data
    @lombok.Builder
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    public static class SchemaStatistics {
        private String applicationName;
        private String serviceName;
        private long totalVersions;
    }

    /**
     * Validate schema content without saving
     *
     * @param content Schema content
     * @return Validation result
     */
    public SchemaValidator.ValidationResult validateSchemaContent(String content) {
        try {
            Map<String, Object> parsedContent = FileUtils.parseContent(content);
            return SchemaValidator.validateSchema(parsedContent, strictValidationMode);
        } catch (Exception e) {
            return SchemaValidator.ValidationResult.failure("Failed to parse schema content: " + e.getMessage());
        }
    }
}