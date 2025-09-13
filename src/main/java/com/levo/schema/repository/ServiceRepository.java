package com.levo.schema.repository;


import com.levo.schema.entity.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Service entity
 *
 * Provides CRUD operations and custom queries for services.
 */
@Repository
public interface ServiceRepository extends JpaRepository<Service, Long> {

    /**
     * Find service by name and application ID
     */
    Optional<Service> findByNameAndApplicationId(String name, Long applicationId);

    /**
     * Find all services for an application
     */
    List<Service> findByApplicationIdOrderByNameAsc(Long applicationId);

    Page<Service> findByApplicationId(Long applicationId, Pageable pageable);

    /**
     * Check if service exists by name and application ID
     */
    boolean existsByNameAndApplicationId(String name, Long applicationId);

    /**
     * Find service by name and application name
     */
    @Query("SELECT s FROM Service s JOIN s.application a WHERE s.name = :serviceName AND a.name = :applicationName")
    Optional<Service> findByNameAndApplicationName(@Param("serviceName") String serviceName,
                                                   @Param("applicationName") String applicationName);

    /**
     * Find service with schemas loaded
     */
    @Query("SELECT s FROM Service s LEFT JOIN FETCH s.schemas WHERE s.id = :id")
    Optional<Service> findByIdWithSchemas(@Param("id") Long id);

    /**
     * Count services by application
     */
    long countByApplicationId(Long applicationId);
}