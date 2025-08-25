package org.example.handler;

import org.example.g2bplatform.exception.ApiHttpException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.LinkedHashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiHttpException.class)
    public ResponseEntity<Map<String, Object>> handleApiHttp(ApiHttpException ex) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("error", ex.getMessage());
        payload.put("status", ex.getStatus());
        payload.put("upstreamBody", ex.getBody() != null && ex.getBody().length() > 1000
                ? ex.getBody().substring(0, 1000) + "...(truncated)"
                : ex.getBody());
        return ResponseEntity.status(502).body(payload);
    }
}
