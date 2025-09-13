package com.levo.schema.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "services",
        uniqueConstraints = @UniqueConstraint(columnNames = {"name", "application_id"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Service {
    @Override
    public String toString() {
        return "Service{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", applicationId=" + applicationId +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", application=" + application +
                ", schemas=" + schemas +
                '}';
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "application_id", nullable = false)
    private Long applicationId;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id", nullable = false, insertable = false, updatable = false)
    private Application application;

    @OneToMany(mappedBy = "service", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Schema> schemas;

    public boolean hasSchemas() {
        return schemas != null && !schemas.isEmpty();
    }

    public Integer getLatestSchemaVersion() {
        if (schemas == null || schemas.isEmpty()) {
            return 0;
        }

        return schemas.stream()
                .mapToInt(Schema::getVersion)
                .max()
                .orElse(0);
    }

    public String getFullyQualifiedName() {
        return (application != null ? application.getName() : "unknown") + "." + name;
    }
}
