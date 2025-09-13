package com.levo.schema.controller;

import com.levo.schema.request.ServiceCreateRequest;
import com.levo.schema.response.MessageResponse;
import com.levo.schema.response.ServiceResponse;
import com.levo.schema.service.ServiceService;
import com.levo.schema.util.Constants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Slf4j
@RestController
@RequestMapping(Constants.API_VERSION + Constants.SERVICES_PATH)
@RequiredArgsConstructor
@Tag(name = "Services", description = "Service management APIs")
public class ServiceController {

    private final ServiceService serviceService;

    @Operation(
            summary = "Create a new service",
            description = "Creates a new service within an application"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Service created successfully"),
            @ApiResponse(responseCode = "409", description = "Service with same name already exists in application"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "404", description = "Application not found")
    })
    @PostMapping
    public ResponseEntity<ServiceResponse> createService(
            @Valid @RequestBody ServiceCreateRequest request) {

        log.info("Received request to create service: {} for application ID: {}",
                request.getName(), request.getApplicationId());

        ServiceResponse response = serviceService.createService(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(
            summary = "Get service by ID",
            description = "Retrieves a service by its unique identifier"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Service found"),
            @ApiResponse(responseCode = "404", description = "Service not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ServiceResponse> getServiceById(
            @Parameter(description = "Service ID") @PathVariable Long id) {

        log.debug("Received request to get service by ID: {}", id);
        ServiceResponse response = serviceService.getServiceById(id);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Get service by name and application",
            description = "Retrieves a service by its name within a specific application"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Service found"),
            @ApiResponse(responseCode = "404", description = "Service or application not found")
    })
    @GetMapping("/name/{serviceName}")
    public ResponseEntity<ServiceResponse> getServiceByName(
            @Parameter(description = "Service name") @PathVariable String serviceName,
            @Parameter(description = "Application name") @RequestParam String applicationName) {

        log.debug("Received request to get service: {} in application: {}", serviceName, applicationName);
        ServiceResponse response = serviceService.getServiceByNameAndApplication(serviceName, applicationName);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "List services by application",
            description = "Retrieves all services within an application with pagination support"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Services retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Application not found")
    })
    @GetMapping("/application/{applicationId}")
    public ResponseEntity<?> listServicesByApplication(
            @Parameter(description = "Application ID") @PathVariable Long applicationId,
            @Parameter(description = "Pagination parameters") Pageable pageable,
            @Parameter(description = "Return all services without pagination")
            @RequestParam(value = "all", required = false, defaultValue = "false") boolean all) {

        log.debug("Received request to list services for application ID: {}, pageable: {}, all: {}",
                applicationId, pageable, all);

        if (all) {
            List<ServiceResponse> response = serviceService.listAllServicesByApplication(applicationId);
            return ResponseEntity.ok(response);
        } else {
            Page<ServiceResponse> response = serviceService.listServicesByApplication(applicationId, pageable);
            return ResponseEntity.ok(response);
        }
    }

    @Operation(
            summary = "Update a service",
            description = "Updates an existing service with new data"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Service updated successfully"),
            @ApiResponse(responseCode = "404", description = "Service not found"),
            @ApiResponse(responseCode = "409", description = "Service with same name already exists in application"),
            @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ServiceResponse> updateService(
            @Parameter(description = "Service ID") @PathVariable Long id,
            @Valid @RequestBody ServiceCreateRequest request) {

        log.info("Received request to update service ID: {}", id);
        ServiceResponse response = serviceService.updateService(id, request);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Delete a service",
            description = "Deletes a service and all its associated schemas"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Service deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Service not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse> deleteService(
            @Parameter(description = "Service ID") @PathVariable Long id) {

        log.info("Received request to delete service ID: {}", id);
        serviceService.deleteService(id);

        MessageResponse response = MessageResponse.success("Service deleted successfully");
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Check if service exists",
            description = "Checks if a service with the given name exists in the specified application"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Check completed")
    })
    @GetMapping("/exists/{serviceName}")
    public ResponseEntity<MessageResponse> checkServiceExists(
            @Parameter(description = "Service name") @PathVariable String serviceName,
            @Parameter(description = "Application ID") @RequestParam Long applicationId) {

        log.debug("Received request to check if service exists: {} in application ID: {}",
                serviceName, applicationId);

        boolean exists = serviceService.serviceExists(serviceName, applicationId);

        MessageResponse response = MessageResponse.success(
                exists ? "Service exists" : "Service does not exist",
                exists
        );
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Count services by application",
            description = "Get the count of services within an application"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Count retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Application not found")
    })
    @GetMapping("/count/application/{applicationId}")
    public ResponseEntity<MessageResponse> countServicesByApplication(
            @Parameter(description = "Application ID") @PathVariable Long applicationId) {

        log.debug("Received request to count services for application ID: {}", applicationId);

        long count = serviceService.countServicesByApplication(applicationId);

        MessageResponse response = MessageResponse.success(
                String.format("Found %d services in application", count),
                count
        );
        return ResponseEntity.ok(response);
    }
}
