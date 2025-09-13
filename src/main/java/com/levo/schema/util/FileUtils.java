package com.levo.schema.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.levo.schema.entity.Schema;
import com.levo.schema.exception.SchemaValidationException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

@Slf4j
@Component
public class FileUtils {

    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();
    private static final ObjectMapper YAML_MAPPER = new ObjectMapper(new YAMLFactory());

    public static Schema.FileFormat detectFileFormat(String content) {
        if (StringUtils.isBlank(content)) {
            throw new SchemaValidationException("Content cannot be empty");
        }

        content = content.trim();

        if (isValidJson(content)) {
            return Schema.FileFormat.JSON;
        }

        if (isValidYaml(content)) {
            return Schema.FileFormat.YAML;
        }

        throw new SchemaValidationException("Content is neither valid JSON nor YAML");
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> parseContent(String content) {
        Schema.FileFormat format = detectFileFormat(content);

        try {
            ObjectMapper mapper = (format == Schema.FileFormat.JSON) ? JSON_MAPPER : YAML_MAPPER;
            return mapper.readValue(content, Map.class);
        } catch (IOException e) {
            throw new SchemaValidationException("Failed to parse content as " + format.name() + ": " + e.getMessage());
        }
    }

    public static String generateFilePath(String basePath, String applicationName,
                                          String serviceName, Integer version,
                                          Schema.FileFormat format) {
        StringBuilder pathBuilder = new StringBuilder(basePath);

        if (!basePath.endsWith("/")) {
            pathBuilder.append("/");
        }

        pathBuilder.append(sanitizeFileName(applicationName));

        if (StringUtils.isNotBlank(serviceName)) {
            pathBuilder.append("/").append(sanitizeFileName(serviceName));
        }

        pathBuilder.append("/")
                .append("schema_")  // You can customize the prefix if needed
                .append(version)
                .append(".")
                .append(format.getExtension());

        return pathBuilder.toString();
    }

    public static void ensureDirectoryExists(String filePath) {
        try {
            Path path = Paths.get(filePath);
            Path directory = path.getParent();
            if (directory != null && !Files.exists(directory)) {
                Files.createDirectories(directory);
                log.debug("Created directory: {}", directory);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to create directory for path: " + filePath, e);
        }
    }

    public static void saveToFile(String content, String filePath) {
        try {
            ensureDirectoryExists(filePath);
            Path path = Paths.get(filePath);
            Files.writeString(path, content);
            log.debug("Saved content to file: {}", filePath);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save content to file: " + filePath, e);
        }
    }

    public static String readFromFile(String filePath) {
        try {
            Path path = Paths.get(filePath);
            if (!Files.exists(path)) {
                throw new RuntimeException("File not found: " + filePath);
            }
            String content = Files.readString(path);
            log.debug("Read content from file: {}", filePath);
            return content;
        } catch (IOException e) {
            throw new RuntimeException("Failed to read content from file: " + filePath, e);
        }
    }

    public static String generateContentHash(String content) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(content.getBytes());
            StringBuilder hexString = new StringBuilder();

            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    private static String sanitizeFileName(String fileName) {
        if (StringUtils.isBlank(fileName)) {
            return "unnamed";
        }

        return fileName.replaceAll("[^a-zA-Z0-9._-]", "_")
                .replaceAll("_{2,}", "_")
                .toLowerCase();
    }

    private static boolean isValidJson(String content) {
        try {
            JsonNode jsonNode = JSON_MAPPER.readTree(content);
            return jsonNode != null;
        } catch (IOException e) {
            return false;
        }
    }

    private static boolean isValidYaml(String content) {
        try {
            JsonNode yamlNode = YAML_MAPPER.readTree(content);
            return yamlNode != null;
        } catch (IOException e) {
            return false;
        }
    }

    public static boolean deleteFile(String filePath) {
        try {
            Path path = Paths.get(filePath);
            boolean deleted = Files.deleteIfExists(path);
            if (deleted) {
                log.debug("Deleted file: {}", filePath);
            }
            return true;
        } catch (IOException e) {
            log.error("Failed to delete file: {}", filePath, e);
            return false;
        }
    }

    public static boolean fileExists(String filePath) {
        return Files.exists(Paths.get(filePath));
    }

    public static long getFileSize(String filePath) {
        try {
            return Files.size(Paths.get(filePath));
        } catch (IOException e) {
            log.error("Failed to get file size: {}", filePath, e);
            return 0;
        }
    }
}
