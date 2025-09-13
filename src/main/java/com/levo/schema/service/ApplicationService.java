package com.levo.schema.service;

import com.levo.schema.request.ApplicationCreateRequest;
import com.levo.schema.response.ApplicationResponse;
import com.levo.schema.mapper.ApplicationMapper;
import com.levo.schema.entity.Application;
import com.levo.schema.exception.DuplicateResourceException;
import com.levo.schema.exception.ResourceNotFoundException;
import com.levo.schema.repository.ApplicationRepository;
import com.levo.schema.util.Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final ApplicationMapper applicationMapper;

    /**
     * Create a new application
     *
     * @param request Application creation request
     * @return Created application response
     */
    @Transactional
    public ApplicationResponse createApplication(ApplicationCreateRequest request) {
        log.info("Creating application: {}", request.getName());

        // Check if application already exists
        if (applicationRepository.existsByNameIgnoreCase(request.getName())) {
            throw new DuplicateResourceException(String.format(Constants.DUPLICATE_APPLICATION, request.getName()));
        }

        // Create and save application
        Application application = applicationMapper.toEntity(request);
        Application savedApplication = applicationRepository.save(application);

        log.info("Successfully created application with ID: {}", savedApplication.getId());
        return applicationMapper.toResponse(savedApplication);
    }

    /**
     * Get application by ID
     *
     * @param id Application ID
     * @return Application response
     */
    @Transactional(readOnly = true)
    public ApplicationResponse getApplicationById(Long id) {
        log.debug("Fetching application by ID: {}", id);

        Application application = applicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found with ID: " + id));

        return applicationMapper.toResponse(application);
    }

    /**
     * Get application by name
     *
     * @param name Application name
     * @return Application response
     */
    @Transactional(readOnly = true)
    public ApplicationResponse getApplicationByName(String name) {
        log.debug("Fetching application by name: {}", name);

        Application application = applicationRepository.findByNameIgnoreCase(name)
                .orElseThrow(() -> new ResourceNotFoundException(String.format(Constants.APPLICATION_NOT_FOUND, name)));

        return applicationMapper.toResponse(application);
    }

    /**
     * List all applications with pagination
     *
     * @param pageable Pagination parameters
     * @return Page of application responses
     */
    @Transactional(readOnly = true)
    public Page<ApplicationResponse> listApplications(Pageable pageable) {
        log.debug("Listing applications with pagination: {}", pageable);

        Page<Application> applications = applicationRepository.findAll(pageable);
        return applications.map(applicationMapper::toResponse);
    }

    /**
     * List all applications
     *
     * @return List of application responses
     */
    @Transactional(readOnly = true)
    public List<ApplicationResponse> listAllApplications() {
        log.debug("Listing all applications");

        List<Application> applications = applicationRepository.findAll();
        return applicationMapper.toResponseList(applications);
    }

    /**
     * Update application
     *
     * @param id Application ID
     * @param request Update request
     * @return Updated application response
     */
    @Transactional
    public ApplicationResponse updateApplication(Long id, ApplicationCreateRequest request) {
        log.info("Updating application with ID: {}", id);

        Application application = applicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found with ID: " + id));

        // Check for name conflicts (excluding current application)
        if (!application.getName().equalsIgnoreCase(request.getName()) &&
                applicationRepository.existsByNameIgnoreCase(request.getName())) {
            throw new DuplicateResourceException(String.format(Constants.DUPLICATE_APPLICATION, request.getName()));
        }

        // Update fields
        application.setName(request.getName());
        application.setDescription(request.getDescription());

        Application updatedApplication = applicationRepository.save(application);
        log.info("Successfully updated application: {}", updatedApplication.getName());

        return applicationMapper.toResponse(updatedApplication);
    }

    /**
     * Delete application
     *
     * @param id Application ID
     */
    @Transactional
    public void deleteApplication(Long id) {
        log.info("Deleting application with ID: {}", id);

        Application application = applicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found with ID: " + id));

        applicationRepository.delete(application);
        log.info("Successfully deleted application: {}", application.getName());
    }

    /**
     * Get or create application by name (internal use)
     *
     * @param name Application name
     * @return Application entity
     */
    @Transactional
    public Application getOrCreateApplicationEntity(String name) {
        log.debug("Getting or creating application: {}", name);

        return applicationRepository.findByNameIgnoreCase(name)
                .orElseGet(() -> {
                    log.info("Auto-creating application: {}", name);
                    Application newApplication = Application.builder()
                            .name(name)
                            .description("Auto-created application for: " + name)
                            .build();
                    return applicationRepository.save(newApplication);
                });
    }

    /**
     * Get application entity by name (internal use)
     *
     * @param name Application name
     * @return Application entity
     */
    @Transactional(readOnly = true)
    public Application getApplicationEntityByName(String name) {
        return applicationRepository.findByNameIgnoreCase(name)
                .orElseThrow(() -> new ResourceNotFoundException(String.format(Constants.APPLICATION_NOT_FOUND, name)));
    }

    /**
     * Check if application exists by name
     *
     * @param name Application name
     * @return true if application exists
     */
    @Transactional(readOnly = true)
    public boolean applicationExists(String name) {
        return applicationRepository.existsByNameIgnoreCase(name);
    }
}