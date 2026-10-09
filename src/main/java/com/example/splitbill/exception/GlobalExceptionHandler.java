package com.example.splitbill.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ResourceNotFoundException.class)
    public org.springframework.http.ResponseEntity<ErrorResponse> notFound(ResourceNotFoundException ex, HttpServletRequest r) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), r);
    }
    @ExceptionHandler(ConflictException.class)
    public org.springframework.http.ResponseEntity<ErrorResponse> conflict(ConflictException ex, HttpServletRequest r) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), r);
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public org.springframework.http.ResponseEntity<ErrorResponse> validation(MethodArgumentNotValidException ex, HttpServletRequest r) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return build(HttpStatus.BAD_REQUEST, message, r);
    }
    @ExceptionHandler(ConstraintViolationException.class)
    public org.springframework.http.ResponseEntity<ErrorResponse> constraint(ConstraintViolationException ex, HttpServletRequest r) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), r);
    }
    @ExceptionHandler(Exception.class)
    public org.springframework.http.ResponseEntity<ErrorResponse> general(Exception ex, HttpServletRequest r) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected server error", r);
    }
    private org.springframework.http.ResponseEntity<ErrorResponse> build(HttpStatus s, String m, HttpServletRequest r) {
        return org.springframework.http.ResponseEntity.status(s)
                .body(new ErrorResponse(LocalDateTime.now(), s.value(), s.getReasonPhrase(), m, r.getRequestURI()));
    }
}
