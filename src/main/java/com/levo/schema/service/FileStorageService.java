package com.levo.schema.service;

import com.levo.schema.entity.Schema;
import com.levo.schema.util.FileUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;
import java.util.Map;

@Slf4j
@Service
public class FileStorageService {

    @Value("${levo.storage.base-path:./schema_storage}")
    private String basePath;

    @PostConstruct
    public void init() {
        log.info("Initializing file storage service with base path: {}", basePath);
        FileUtils.ensureDirectoryExists(basePath + "/temp");
    }

    /**
     * Save schema content to file
     *
     * @param content Schema content as string
     * @param applicationName Application name
     * @param serviceName Service name (optional)
     * @param version Schema version
     * @param format File format
     * @return File path where content was saved
     */
    public String saveSchema(String content, String applicationName, String serviceName,
                             Integer version, Schema.FileFormat format) {
        try {
            String filePath = FileUtils.generateFilePath(basePath, applicationName, serviceName, version, format);
            FileUtils.saveToFile(content, filePath);
            log.info("Saved schema to file: {}", filePath);
            return filePath;
        } catch (Exception e) {
            log.error("Failed to save schema file", e);
            throw new RuntimeException("Failed to save schema file: " + e.getMessage(), e);
        }
    }

    /**
     * Load schema content from file
     *
     * @param filePath File path to read from
     * @return Parsed schema content as Map
     */
    public Map<String, Object> loadSchema(String filePath) {
        try {
            String content = FileUtils.readFromFile(filePath);
            Map<String, Object> parsedContent = FileUtils.parseContent(content);
            log.debug("Loaded schema from file: {}", filePath);
            return parsedContent;
        } catch (Exception e) {
            log.error("Failed to load schema file: {}", filePath, e);
            throw new RuntimeException("Failed to load schema file: " + e.getMessage(), e);
        }
    }

    /**
     * Load raw schema content from file
     *
     * @param filePath File path to read from
     * @return Raw schema content as string
     */
    public String getSchemaFileContent(String filePath) {
        try {
            String content = FileUtils.readFromFile(filePath);
            log.debug("Loaded raw schema from file: {}", filePath);
            return content;
        } catch (Exception e) {
            log.error("Failed to load raw schema file: {}", filePath, e);
            throw new RuntimeException("Failed to load raw schema file: " + e.getMessage(), e);
        }
    }

    /**
     * Delete schema file
     *
     * @param filePath File path to delete
     * @return true if file was deleted successfully
     */
    public boolean deleteSchema(String filePath) {
        try {
            boolean deleted = FileUtils.deleteFile(filePath);
            if (deleted) {
                log.info("Deleted schema file: {}", filePath);
            } else {
                log.warn("Failed to delete schema file: {}", filePath);
            }
            return deleted;
        } catch (Exception e) {
            log.error("Error deleting schema file: {}", filePath, e);
            return false;
        }
    }

    /**
     * Check if schema file exists
     *
     * @param filePath File path to check
     * @return true if file exists
     */
    public boolean schemaExists(String filePath) {
        return FileUtils.fileExists(filePath);
    }

    /**
     * Get file size
     *
     * @param filePath File path
     * @return File size in bytes
     */
    public long getFileSize(String filePath) {
        return FileUtils.getFileSize(filePath);
    }

    /**
     * Generate content hash
     *
     * @param content Content to hash
     * @return SHA-256 hash
     */
    public String generateContentHash(String content) {
        return FileUtils.generateContentHash(content);
    }

    /**
     * Get storage base path
     *
     * @return Base storage path
     */
    public String getBasePath() {
        return basePath;
    }
}
