package com.levo.schema.controller;

import com.levo.schema.request.ApplicationCreateRequest;
import com.levo.schema.response.ApplicationResponse;
import com.levo.schema.response.MessageResponse;
import com.levo.schema.service.ApplicationService;
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
@RequestMapping(Constants.API_VERSION + Constants.APPLICATIONS_PATH)
@RequiredArgsConstructor
@Tag(name = "Applications", description = "Application management APIs")
public class ApplicationController {

    private final ApplicationService applicationService;

    @Operation(
            summary = "Create a new application",
            description = "Creates a new application with the provided name and description"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Application created successfully"),
            @ApiResponse(responseCode = "409", description = "Application with same name already exists"),
            @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    @PostMapping
    public ResponseEntity<ApplicationResponse> createApplication(
            @Valid @RequestBody ApplicationCreateRequest request) {

        log.info("Received request to create application: {}", request.getName());
        ApplicationResponse response = applicationService.createApplication(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(
            summary = "Get application by ID",
            description = "Retrieves an application by its unique identifier"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Application found"),
            @ApiResponse(responseCode = "404", description = "Application not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApplicationResponse> getApplicationById(
            @Parameter(description = "Application ID") @PathVariable Long id) {

        log.debug("Received request to get application by ID: {}", id);
        ApplicationResponse response = applicationService.getApplicationById(id);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Get application by name",
            description = "Retrieves an application by its name (case-insensitive)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Application found"),
            @ApiResponse(responseCode = "404", description = "Application not found")
    })
    @GetMapping("/name/{name}")
    public ResponseEntity<ApplicationResponse> getApplicationByName(
            @Parameter(description = "Application name") @PathVariable String name) {

        log.debug("Received request to get application by name: {}", name);
        ApplicationResponse response = applicationService.getApplicationByName(name);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "List all applications",
            description = "Retrieves all applications with pagination support"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Applications retrieved successfully")
    })
    @GetMapping
    public ResponseEntity<?> listApplications(
            @Parameter(description = "Pagination parameters") Pageable pageable,
            @Parameter(description = "Return all applications without pagination")
            @RequestParam(value = "all", required = false, defaultValue = "false") boolean all) {

        log.debug("Received request to list applications, pageable: {}, all: {}", pageable, all);

        if (all) {
            List<ApplicationResponse> response = applicationService.listAllApplications();
            return ResponseEntity.ok(response);
        } else {
            Page<ApplicationResponse> response = applicationService.listApplications(pageable);
            return ResponseEntity.ok(response);
        }
    }

    @Operation(
            summary = "Update an application",
            description = "Updates an existing application with new data"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Application updated successfully"),
            @ApiResponse(responseCode = "404", description = "Application not found"),
            @ApiResponse(responseCode = "409", description = "Application with same name already exists"),
            @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ApplicationResponse> updateApplication(
            @Parameter(description = "Application ID") @PathVariable Long id,
            @Valid @RequestBody ApplicationCreateRequest request) {

        log.info("Received request to update application ID: {}", id);
        ApplicationResponse response = applicationService.updateApplication(id, request);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Delete an application",
            description = "Deletes an application and all its associated data"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Application deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Application not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse> deleteApplication(
            @Parameter(description = "Application ID") @PathVariable Long id) {

        log.info("Received request to delete application ID: {}", id);
        applicationService.deleteApplication(id);

        MessageResponse response = MessageResponse.success("Application deleted successfully");
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Check if application exists",
            description = "Checks if an application with the given name exists"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Check completed")
    })
    @GetMapping("/exists/{name}")
    public ResponseEntity<MessageResponse> checkApplicationExists(
            @Parameter(description = "Application name") @PathVariable String name) {

        log.debug("Received request to check if application exists: {}", name);
        boolean exists = applicationService.applicationExists(name);

        MessageResponse response = MessageResponse.success(
                exists ? "Application exists" : "Application does not exist",
                exists
        );
        return ResponseEntity.ok(response);
    }
}
