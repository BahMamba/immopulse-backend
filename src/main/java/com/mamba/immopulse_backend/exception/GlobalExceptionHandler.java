package com.mamba.immopulse_backend.exception;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class); // 👈 Ajoute un logger

    // ⚠️ Identifiants invalides
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleBadCredentials(BadCredentialsException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.UNAUTHORIZED.value());
        body.put("message", "Identifiants invalides !");
        return new ResponseEntity<>(body, HttpStatus.UNAUTHORIZED);
    }

    // ⚠️ Erreurs métier (Exception volontaire dans le service ou controller)
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntime(RuntimeException ex) {
        logger.warn("Erreur métier : {}", ex.getMessage()); // 👈 Log sur niveau WARN
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("message", ex.getMessage());
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    // ⚠️ Mauvais format DTO (validation échouée)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.UNPROCESSABLE_ENTITY.value());
        body.put("message", "Données invalides dans la requête !");
        return new ResponseEntity<>(body, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    // ❗️ Erreurs techniques inattendues (null, file, etc.)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGlobal(Exception ex) {
        logger.error("Erreur interne non prévue :", ex); // 👈 Ceci logge la stack trace en console

        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        body.put("message", "Une erreur inattendue est survenue !");
        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
