package com.levo.schema.service;

import com.levo.schema.request.ServiceCreateRequest;
import com.levo.schema.response.ApplicationResponse;
import com.levo.schema.response.ServiceResponse;
import com.levo.schema.mapper.ServiceMapper;
import com.levo.schema.entity.Application;
import com.levo.schema.entity.Service;
import com.levo.schema.exception.DuplicateResourceException;
import com.levo.schema.exception.ResourceNotFoundException;
import com.levo.schema.repository.ServiceRepository;
import com.levo.schema.util.Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ServiceService {

    private final ServiceRepository serviceRepository;
    private final ServiceMapper serviceMapper;
    private final ApplicationService applicationService;

    /**
     * Create a new service
     *
     * @param request Service creation request
     * @return Created service response
     */
    @Transactional
    public ServiceResponse createService(ServiceCreateRequest request) {
        log.info("Creating service: {} for application ID: {}", request.getName(), request.getApplicationId());

        applicationService.getApplicationById(request.getApplicationId());

        if (serviceRepository.existsByNameAndApplicationId(request.getName(), request.getApplicationId())) {
            throw new DuplicateResourceException(
                    String.format("Service '%s' already exists in application with ID: %d",
                            request.getName(), request.getApplicationId()));
        }

        Service service = serviceMapper.toEntity(request);
        Service savedService = serviceRepository.save(service);

        log.info("Successfully created service with ID: {}", savedService.getId());
        return serviceMapper.toResponse(savedService);
    }

    /**
     * Get service by ID
     *
     * @param id Service ID
     * @return Service response
     */
    @Transactional(readOnly = true)
    public ServiceResponse getServiceById(Long id) {
        log.debug("Fetching service by ID: {}", id);

        Service service = serviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Service not found with ID: " + id));

        return serviceMapper.toResponse(service);
    }

    /**
     * Get service by name and application name
     *
     * @param serviceName Service name
     * @param applicationName Application name
     * @return Service response
     */
    @Transactional(readOnly = true)
    public ServiceResponse getServiceByNameAndApplication(String serviceName, String applicationName) {
        log.debug("Fetching service: {} for application: {}", serviceName, applicationName);

        Service service = serviceRepository.findByNameAndApplicationName(serviceName, applicationName)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("Service '%s' not found in application '%s'", serviceName, applicationName)));

        return serviceMapper.toResponse(service);
    }

    /**
     * List services by application ID
     *
     * @param applicationId Application ID
     * @param pageable Pagination parameters
     * @return Page of service responses
     */
    @Transactional(readOnly = true)
    public Page<ServiceResponse> listServicesByApplication(Long applicationId, Pageable pageable) {
        log.debug("Listing services for application ID: {} with pagination: {}", applicationId, pageable);

        // Verify application exists
        applicationService.getApplicationById(applicationId);

        Page<Service> services = serviceRepository.findByApplicationId(applicationId, pageable);
        return services.map(serviceMapper::toResponse);
    }

    /**
     * List all services by application ID
     *
     * @param applicationId Application ID
     * @return List of service responses
     */
    @Transactional(readOnly = true)
    public List<ServiceResponse> listAllServicesByApplication(Long applicationId) {
        log.debug("Listing all services for application ID: {}", applicationId);

        // Verify application exists
        applicationService.getApplicationById(applicationId);

        List<Service> services = serviceRepository.findByApplicationIdOrderByNameAsc(applicationId);
        return serviceMapper.toResponseList(services);
    }

    /**
     * Update service
     *
     * @param id Service ID
     * @param request Update request
     * @return Updated service response
     */
    @Transactional
    public ServiceResponse updateService(Long id, ServiceCreateRequest request) {
        log.info("Updating service with ID: {}", id);

        Service service = serviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Service not found with ID: " + id));

        applicationService.getApplicationById(request.getApplicationId());

        if (!service.getName().equalsIgnoreCase(request.getName()) &&
                serviceRepository.existsByNameAndApplicationId(request.getName(), service.getApplicationId())) {
            throw new DuplicateResourceException(
                    String.format("Service '%s' already exists in this application", request.getName()));
        }

        service.setName(request.getName());
        service.setDescription(request.getDescription());

        Service updatedService = serviceRepository.save(service);
        log.info("Successfully updated service: {}", updatedService.getName());

        return serviceMapper.toResponse(updatedService);
    }

    /**
     * Delete service
     *
     * @param id Service ID
     */
    @Transactional
    public void deleteService(Long id) {
        log.info("Deleting service with ID: {}", id);

        Service service = serviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Service not found with ID: " + id));

        serviceRepository.delete(service);
        log.info("Successfully deleted service: {}", service.getName());
    }

    /**
     * Get or create service by name and application (internal use)
     *
     * @param serviceName Service name
     * @param application Application entity
     * @return Service entity
     */
    @Transactional
    public Service getOrCreateServiceEntity(String serviceName, Application application) {
        log.debug("Getting or creating service: {} for application: {}", serviceName, application.getName());

        return serviceRepository.findByNameAndApplicationId(serviceName, application.getId())
                .orElseGet(() -> {
                    log.info("Auto-creating service: {} for application: {}", serviceName, application.getName());
                    Service newService = Service.builder()
                            .name(serviceName)
                            .applicationId(application.getId())
                            .description("Auto-created service for: " + serviceName)
                            .build();
                    return serviceRepository.save(newService);
                });
    }

    /**
     * Get service entity by name and application name (internal use)
     *
     * @param serviceName Service name
     * @param applicationName Application name
     * @return Service entity
     */
    @Transactional(readOnly = true)
    public Service getServiceEntityByNameAndApplication(String serviceName, String applicationName) {
        return serviceRepository.findByNameAndApplicationName(serviceName, applicationName)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format(Constants.SERVICE_NOT_FOUND, serviceName + " in application " + applicationName)));
    }

    /**
     * Check if service exists by name and application
     *
     * @param serviceName Service name
     * @param applicationId Application ID
     * @return true if service exists
     */
    @Transactional(readOnly = true)
    public boolean serviceExists(String serviceName, Long applicationId) {
        return serviceRepository.existsByNameAndApplicationId(serviceName, applicationId);
    }

    /**
     * Count services by application
     *
     * @param applicationId Application ID
     * @return Service count
     */
    @Transactional(readOnly = true)
    public long countServicesByApplication(Long applicationId) {
        return serviceRepository.countByApplicationId(applicationId);
    }
}
