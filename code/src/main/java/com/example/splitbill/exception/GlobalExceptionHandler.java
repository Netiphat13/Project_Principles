package com.example.splitbill.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

/**
 * จัดการ error เฉพาะ REST API (controller/api) — หน้าเว็บ Thymeleaf ใช้ error page ปกติของ Spring
 */
@RestControllerAdvice(basePackages = "com.example.splitbill.controller.api")
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> notFound(ResourceNotFoundException ex, HttpServletRequest r) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), r);
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ErrorResponse> forbidden(ForbiddenException ex, HttpServletRequest r) {
        return build(HttpStatus.FORBIDDEN, ex.getMessage(), r);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponse> conflict(ConflictException ex, HttpServletRequest r) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), r);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> dataIntegrity(DataIntegrityViolationException ex, HttpServletRequest r) {
        return build(HttpStatus.CONFLICT, "Operation conflicts with existing data", r);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> validation(MethodArgumentNotValidException ex, HttpServletRequest r) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return build(HttpStatus.BAD_REQUEST, message, r);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> constraint(ConstraintViolationException ex, HttpServletRequest r) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), r);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ErrorResponse> badRequest(Exception ex, HttpServletRequest r) {
        return build(HttpStatus.BAD_REQUEST, "Malformed request", r);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> methodNotAllowed(HttpRequestMethodNotSupportedException ex, HttpServletRequest r) {
        return build(HttpStatus.METHOD_NOT_ALLOWED, ex.getMessage(), r);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> general(Exception ex, HttpServletRequest r) {
        log.error("Unhandled error on {} {}", r.getMethod(), r.getRequestURI(), ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected server error", r);
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus s, String m, HttpServletRequest r) {
        return ResponseEntity.status(s)
                .body(new ErrorResponse(LocalDateTime.now(), s.value(), s.getReasonPhrase(), m, r.getRequestURI()));
    }
}
