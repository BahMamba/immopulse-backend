package com.mamba.immopulse_backend.exception;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Gestion des identifiants invalides
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleBadCrendentials(BadCredentialsException exception){
        Map<String, Object> jsonBody = new HashMap<>();
        jsonBody.put("status", HttpStatus.UNAUTHORIZED.value());
        jsonBody.put("message", "Identifiants Invalides!");
        return new ResponseEntity<>(jsonBody, HttpStatus.UNAUTHORIZED);

    }

    // Gestion des erreurs simples
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntime(RuntimeException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("message", ex.getMessage());
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }
}
