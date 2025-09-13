package com.levo.schema.response;

@lombok.Data
@lombok.Builder
@lombok.AllArgsConstructor
@lombok.NoArgsConstructor
public class ValidationResponse {
    private boolean valid;
    private String message;
    private java.util.List<String> warnings;
}
