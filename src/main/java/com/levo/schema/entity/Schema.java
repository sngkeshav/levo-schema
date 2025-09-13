package com.levo.schema.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDateTime;

@Entity
@Table(name = "schemas",
        uniqueConstraints = @UniqueConstraint(columnNames = {"application_id", "service_id", "version"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Schema {
    @Override
    public String toString() {
        return "Schema{" +
                "id=" + id +
                ", applicationId=" + applicationId +
                ", serviceId=" + serviceId +
                ", version=" + version +
                ", filePath='" + filePath + '\'' +
                ", fileFormat=" + fileFormat +
                ", isLatest=" + isLatest +
                ", content='" + content + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", application=" + application +
                ", service=" + service +
                '}';
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "application_id", nullable = false)
    private Long applicationId;

    @Column(name = "service_id")
    private Long serviceId;

    @Column(name = "version", nullable = false)
    private Integer version;

    @Column(name = "file_path", nullable = false, length = 500)
    private String filePath;

    @Column(name = "file_format", nullable = false, length = 10)
    @Enumerated(EnumType.STRING)
    private FileFormat fileFormat;

    @Column(name = "is_latest", nullable = false)
    @Builder.Default
    private Boolean isLatest = true;

    @Column(name = "content")
    private String content;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false, updatable = true)
    private LocalDateTime updatedAt;

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id", nullable = false, insertable = false, updatable = false)
    @JsonIgnore
    private Application application;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id", insertable = false, updatable = false)
    @JsonIgnore
    private Service service;

    public enum FileFormat {
        JSON("json"),
        YAML("yaml");

        private final String extension;

        FileFormat(String extension) {
            this.extension = extension;
        }

        public String getExtension() {
            return extension;
        }

        public static FileFormat fromExtension(String extension) {
            for (FileFormat format : values()) {
                if (format.extension.equalsIgnoreCase(extension)) {
                    return format;
                }
            }
            throw new IllegalArgumentException("Unsupported file format: " + extension);
        }
    }

    public boolean isApplicationLevel() {
        return serviceId == null;
    }

    public boolean isServiceLevel() {
        return serviceId != null;
    }

    public String getScopeIdentifier() {
        if (application == null) {
            return "unknown";
        }

        if (isApplicationLevel()) {
            return application.getName();
        } else if (service != null) {
            return application.getName() + "." + service.getName();
        } else {
            return application.getName() + ".unknown-service";
        }
    }

    public String getFileName() {
        if (filePath == null) {
            return null;
        }

        int lastSlash = filePath.lastIndexOf('/');
        return lastSlash >= 0 ? filePath.substring(lastSlash + 1) : filePath;
    }
}