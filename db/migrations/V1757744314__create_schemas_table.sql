CREATE TABLE schemas (
    id BIGSERIAL PRIMARY KEY,
    application_id BIGINT NOT NULL,
    service_id BIGINT NULL,
    version INTEGER NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    file_format VARCHAR(10) NOT NULL CHECK (file_format IN ('JSON', 'YAML')),
    is_latest BOOLEAN NOT NULL DEFAULT true,
    content TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_schemas_application_id
        FOREIGN KEY (application_id)
        REFERENCES applications(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_schemas_service_id
        FOREIGN KEY (service_id)
        REFERENCES services(id)
        ON DELETE CASCADE,

    CONSTRAINT uk_schemas_version_scope
        UNIQUE (application_id, service_id, version)
);