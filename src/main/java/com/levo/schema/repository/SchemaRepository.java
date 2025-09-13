package com.levo.schema.repository;

import com.levo.schema.entity.Schema;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface SchemaRepository extends JpaRepository<Schema, Long> {

    Optional<Schema> findByApplicationIdAndServiceIdIsNullAndIsLatestTrue(Long applicationId);

    Optional<Schema> findByApplicationIdAndServiceIdAndIsLatestTrue(Long applicationId, Long serviceId);

    Optional<Schema> findByApplicationIdAndServiceIdIsNullAndVersion(Long applicationId, Integer version);

    Optional<Schema> findByApplicationIdAndServiceIdAndVersion(Long applicationId, Long serviceId, Integer version);

    Page<Schema> findByApplicationIdAndServiceIdIsNullOrderByVersionDesc(Long applicationId, Pageable pageable);

    Page<Schema> findByApplicationIdAndServiceIdOrderByVersionDesc(Long applicationId, Long serviceId, Pageable pageable);

    Optional<Schema> findFirstByApplicationIdAndServiceIdOrderByVersionDesc(Long applicationId, Long serviceId);
    Optional<Schema> findFirstByApplicationIdAndServiceIdIsNullOrderByVersionDesc(Long applicationId);

    @Query(
        """
            SELECT COALESCE(MAX(s.version), 0) + 1
            FROM Schema s
            WHERE s.applicationId = :applicationId
            AND s.serviceId IS NULL
        """
    )
    Integer getNextVersionForApplication(@Param("applicationId") Long applicationId);

    @Query(
        """
            SELECT COALESCE(MAX(s.version), 0) + 1
            FROM Schema s
            WHERE s.applicationId = :applicationId
            AND s.serviceId = :serviceId
        """
    )
    Integer getNextVersionForService(
            @Param("applicationId") Long applicationId,
            @Param("serviceId") Long serviceId
    );

    @Modifying
    @Query(
        """
            UPDATE Schema s SET s.isLatest = false
            WHERE s.applicationId = :applicationId
            AND s.serviceId IS NULL
        """
    )
    void markAllAsNotLatestForApplication(@Param("applicationId") Long applicationId);

    @Modifying
    @Query(
        """
            UPDATE Schema s SET s.isLatest = false
            WHERE s.applicationId = :applicationId
            AND s.serviceId = :serviceId
        """
    )
    void markAllAsNotLatestForService(
            @Param("applicationId") Long applicationId,
            @Param("serviceId") Long serviceId
    );

    long countByApplicationId(Long applicationId);

    long countByServiceId(Long serviceId);

    @Query(
        """
            SELECT s
            FROM Schema s
            JOIN s.application a
            WHERE a.name = :applicationName
            ORDER BY s.version DESC
        """
    )
    List<Schema> findByApplicationName(@Param("applicationName") String applicationName);

    @Query(
        """
            SELECT s
            FROM Schema s
            JOIN s.application a
            JOIN s.service srv
            WHERE a.name = :applicationName
            AND srv.name = :serviceName
            ORDER BY s.version DESC
        """
    )
    List<Schema> findByApplicationNameAndServiceName(
        @Param("applicationName") String applicationName,
        @Param("serviceName") String serviceName
    );

    @Query(
        """
            SELECT s
            FROM Schema s
            JOIN s.application a
            WHERE a.name = :applicationName
            AND s.serviceId IS NULL
            AND s.isLatest = true
        """
    )
    Optional<Schema> findLatestByApplicationName(@Param("applicationName") String applicationName);

    @Query(
        """
            SELECT s
            FROM Schema s
            JOIN s.application a
            JOIN s.service srv
            WHERE a.name = :applicationName
            AND srv.name = :serviceName
            AND s.isLatest = true
        """
    )
    Optional<Schema> findLatestByApplicationNameAndServiceName(
        @Param("applicationName") String applicationName,
        @Param("serviceName") String serviceName
    );

    @Query(
        """
            SELECT s
            FROM Schema s
            JOIN s.application a
            JOIN s.service srv
            WHERE a.name = :applicationName
            AND srv.name = :serviceName
            AND s.version = :version
        """
    )
    Optional<Schema> findByApplicationNameAndServiceNameAndVersion(
        @Param("applicationName") String applicationName,
        @Param("serviceName") String serviceName,
        @Param("version") Integer version
    );

    @Query(
        """
            SELECT s
            FROM Schema s
            JOIN s.application a
            WHERE a.name = :applicationName
            AND s.serviceId IS NULL
            AND s.version = :version
        """
    )
    Optional<Schema> findByApplicationNameAndVersion(
        @Param("applicationName") String applicationName,
        @Param("version") Integer version
    );
}

