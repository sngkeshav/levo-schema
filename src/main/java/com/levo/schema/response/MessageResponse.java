package com.levo.schema.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageResponse {
    private String message;
    private String status;
    private LocalDateTime timestamp;
    private Object data;

    public MessageResponse(String message) {
        this.message = message;
        this.status = "success";
        this.timestamp = LocalDateTime.now();
    }

    public MessageResponse(String message, String status) {
        this.message = message;
        this.status = status;
        this.timestamp = LocalDateTime.now();
    }

    public static MessageResponse success(String message) {
        return MessageResponse.builder()
                .message(message)
                .status("success")
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static MessageResponse success(String message, Object data) {
        return MessageResponse.builder()
                .message(message)
                .status("success")
                .timestamp(LocalDateTime.now())
                .data(data)
                .build();
    }
}
