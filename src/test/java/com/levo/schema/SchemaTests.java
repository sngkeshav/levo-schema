package com.levo.schema;

import com.levo.schema.response.SchemaResponse;
import com.levo.schema.entity.Application;
import com.levo.schema.entity.Schema;
import com.levo.schema.entity.Service;
import com.levo.schema.repository.ApplicationRepository;
import com.levo.schema.repository.SchemaRepository;
import com.levo.schema.repository.ServiceRepository;
import com.levo.schema.service.ApplicationService;
import com.levo.schema.service.SchemaService;
import com.levo.schema.service.ServiceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest
@ActiveProfiles("test")
@Transactional
class SchemaTests {

    @Autowired
    private SchemaService schemaService;

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private ServiceService serviceService;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private ServiceRepository serviceRepository;

    @Autowired
    private SchemaRepository schemaRepository;

    @TempDir
    Path tempDir;

    private final String VALID_OPENAPI_JSON = """
            {
              "openapi": "3.0.0",
              "info": {
                "title": "Test API",
                "version": "1.0.0",
                "description": "A test API for schema upload testing"
              },
              "paths": {
                "/users": {
                  "get": {
                    "summary": "Get all users",
                    "responses": {
                      "200": {
                        "description": "List of users",
                        "content": {
                          "application/json": {
                            "schema": {
                              "type": "array",
                              "items": {
                                "type": "object",
                                "properties": {
                                  "id": {"type": "integer"},
                                  "name": {"type": "string"},
                                  "email": {"type": "string"}
                                }
                              }
                            }
                          }
                        }
                      }
                    }
                  }
                }
              }
            }
            """;

    private final String VALID_OPENAPI_YAML = """
            openapi: 3.0.0
            info:
              title: Product API
              version: 2.0.0
              description: A test API for product management
            paths:
              /products:
                get:
                  summary: Get all products
                  responses:
                    '200':
                      description: List of products
                      content:
                        application/json:
                          schema:
                            type: array
                            items:
                              type: object
                              properties:
                                id:
                                  type: integer
                                name:
                                  type: string
                                price:
                                  type: number
            """;

    @BeforeEach
    void setUp() {
        // Clean up any existing data
        schemaRepository.deleteAll();
        serviceRepository.deleteAll();
        applicationRepository.deleteAll();
    }

    @Test
    void uploadSchema_NewApplicationAndService_ShouldCreateBothAndSchema() {
        String applicationName = "ecommerce-api";
        String serviceName = "user-service";
        MockMultipartFile file = new MockMultipartFile(
                "schema", "openapi.json", "application/json", VALID_OPENAPI_JSON.getBytes());

        // When
        SchemaResponse response = schemaService.uploadSchemaFromFile(file, applicationName, serviceName);

        // Then
        // Verify response
        assertThat(response).isNotNull();
        assertThat(response.getVersion()).isEqualTo(1);
        assertThat(response.getFileFormat()).isEqualTo(Schema.FileFormat.JSON);
        assertThat(response.getIsLatest()).isTrue();
        assertThat(response.getContent()).isNotNull();

        // Verify application was created
        List<Application> applications = applicationRepository.findAll();
        assertThat(applications).hasSize(1);
        Application createdApplication = applications.get(0);
        assertThat(createdApplication.getName()).isEqualTo(applicationName);
        assertThat(createdApplication.getDescription()).contains("Auto-created application");

        // Verify service was created
        List<Service> services = serviceRepository.findAll();
        assertThat(services).hasSize(1);
        Service createdService = services.get(0);
        assertThat(createdService.getName()).isEqualTo(serviceName);
        assertThat(createdService.getApplicationId()).isEqualTo(createdApplication.getId());
        assertThat(createdService.getDescription()).contains("Auto-created service");

        // Verify schema was created
        List<Schema> schemas = schemaRepository.findAll();
        assertThat(schemas).hasSize(1);
        Schema createdSchema = schemas.get(0);
        assertThat(createdSchema.getApplicationId()).isEqualTo(createdApplication.getId());
        assertThat(createdSchema.getServiceId()).isEqualTo(createdService.getId());
        assertThat(createdSchema.getVersion()).isEqualTo(1);
        assertThat(createdSchema.getIsLatest()).isTrue();
        assertThat(createdSchema.getFilePath()).isNotNull();
        assertThat(createdSchema.getContent()).isNotNull();
    }

    @Test
    void uploadSchema_NewApplicationOnly_ShouldCreateApplicationAndSchema() {
        // Given
        String applicationName = "payment-api";
        MockMultipartFile file = new MockMultipartFile(
                "schema", "openapi.yaml", "application/yaml", VALID_OPENAPI_YAML.getBytes());

        // When - Upload without service name
        SchemaResponse response = schemaService.uploadSchemaFromFile(file, applicationName, null);

        // Then
        // Verify response
        assertThat(response).isNotNull();
        assertThat(response.getVersion()).isEqualTo(1);
        assertThat(response.getFileFormat()).isEqualTo(Schema.FileFormat.YAML);
        assertThat(response.getServiceId()).isNull();
        assertThat(response.getServiceName()).isNull();

        // Verify application was created
        List<Application> applications = applicationRepository.findAll();
        assertThat(applications).hasSize(1);
        Application createdApplication = applications.get(0);
        assertThat(createdApplication.getName()).isEqualTo(applicationName);

        // Verify no services were created
        List<Service> services = serviceRepository.findAll();
        assertThat(services).isEmpty();

        // Verify schema was created at application level
        List<Schema> schemas = schemaRepository.findAll();
        assertThat(schemas).hasSize(1);
        Schema createdSchema = schemas.get(0);
        assertThat(createdSchema.getApplicationId()).isEqualTo(createdApplication.getId());
        assertThat(createdSchema.getServiceId()).isNull(); // Application-level schema
        assertThat(createdSchema.getVersion()).isEqualTo(1);
        assertThat(createdSchema.getIsLatest()).isTrue();
    }

    @Test
    void uploadSchema_SeparateApplicationsAndServices_ShouldMaintainIndependentVersioning() {
        // Given
        String app1 = "app1";
        String app2 = "app2";
        String service1 = "service1";
        String service2 = "service2";

        // When - Upload schemas for different scopes
        MockMultipartFile file1 = new MockMultipartFile(
                "schema", "file1.json", "application/json", VALID_OPENAPI_JSON.getBytes());
        MockMultipartFile file2 = new MockMultipartFile(
                "schema", "file2.json", "application/json", VALID_OPENAPI_JSON.getBytes());
        MockMultipartFile file3 = new MockMultipartFile(
                "schema", "file3.json", "application/json", VALID_OPENAPI_JSON.getBytes());
        MockMultipartFile file4 = new MockMultipartFile(
                "schema", "file4.json", "application/json", VALID_OPENAPI_JSON.getBytes());

        SchemaResponse resp1 = schemaService.uploadSchemaFromFile(file1, app1, service1);
        SchemaResponse resp2 = schemaService.uploadSchemaFromFile(file2, app1, service2);
        SchemaResponse resp3 = schemaService.uploadSchemaFromFile(file3, app2, service1);
        SchemaResponse resp4 = schemaService.uploadSchemaFromFile(file4, app1, null);

        assertThat(resp1.getVersion()).isEqualTo(1);
        assertThat(resp2.getVersion()).isEqualTo(1);
        assertThat(resp3.getVersion()).isEqualTo(1);
        assertThat(resp4.getVersion()).isEqualTo(1);

        List<Application> applications = applicationRepository.findAll();
        assertThat(applications).hasSize(2);

        List<Service> services = serviceRepository.findAll();
        assertThat(services).hasSize(3);

        List<Schema> schemas = schemaRepository.findAll();
        assertThat(schemas).hasSize(4);

        long latestCount = schemas.stream().filter(Schema::getIsLatest).count();
        assertThat(latestCount).isEqualTo(4);
    }
}