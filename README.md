# Levo Schema Management API

A simple Spring Boot REST API for managing OpenAPI schemas with versioning support.

---

## 🚀 Features

- Upload and validate OpenAPI 3.x schemas (JSON/YAML)
- Automatic versioning of schemas
- Store schema metadata in PostgreSQL
- Clean REST endpoints
- OpenAPI documentation (Swagger)

---

## ⚡️ Tech Stack

- Spring Boot
- Java 17
- PostgreSQL
- Spring Data JPA
- SpringDoc (Swagger)

---

## ✅ Quick Start

1. Start PostgreSQL (using Docker recommended):
    ```bash
    docker-compose up -d
    ```

2. Access API documentation (Swagger UI):
    ```
    http://localhost:8080/api/swagger-ui/index.html
    ```

---

## 📦 Example API Usage

### Create Application
```bash
curl -X 'POST' \
  'http://localhost:8080/api/v1/applications' \
  -H 'accept: */*' \
  -H 'Content-Type: application/json' \
  -d '{
  "name": "Testing",
  "description": "testing"
}'
```
### Upload Schema -> If the provided application name or service name does not exist, this endpoint automatically creates those entries with the given names.
```bash
curl -X 'POST' \
  'http://localhost:8080/api/v1/schemas/upload' \
  -H 'accept: */*' \
  -H 'Content-Type: multipart/form-data' \
  -F 'file=@openapi.yaml;type=application/x-yaml' \
  -F 'application=testing' \
  -F 'service=testing'
```