package com.levo.schema.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.mapping.Any;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorResponse {
    private String error;
    private String message;
    private Integer status;
    private LocalDateTime timestamp;
    private String path;
    private Map<String, Object> details;

    public ErrorResponse(String error, String message, Integer status) {
        this.error = error;
        this.message = message;
        this.status = status;
        this.timestamp = LocalDateTime.now();
    }

    public static ErrorResponse of(String error, String message, Integer status) {
        return ErrorResponse.builder()
                .error(error)
                .message(message)
                .status(status)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
