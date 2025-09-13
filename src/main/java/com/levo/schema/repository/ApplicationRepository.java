package com.levo.schema.repository;

import com.levo.schema.entity.Application;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {

    Optional<Application> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);
}