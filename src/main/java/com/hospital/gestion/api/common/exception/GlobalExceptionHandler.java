package com.hospital.gestion.api.common.exception;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;


import org.springframework.security.access.AccessDeniedException;

import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import tools.jackson.databind.exc.InvalidFormatException;

import org.springframework.web.servlet.resource.NoResourceFoundException;


import org.springframework.security.core.AuthenticationException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // ============================================================
    // NOT FOUND
    // ============================================================

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse>
    handleResourceNotFoundException(
            ResourceNotFoundException exception
    ) {
        return buildResponse(
                HttpStatus.NOT_FOUND,
                exception.getMessage()
        );
    }

    // ============================================================
    // CONFLICT
    // ============================================================

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponse>
    handleConflictException(
            ConflictException exception
    ) {
        return buildResponse(
                HttpStatus.CONFLICT,
                exception.getMessage()
        );
    }

    // ============================================================
    // ILLEGAL ARGUMENT
    // ============================================================

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse>
    handleIllegalArgumentException(
            IllegalArgumentException exception
    ) {
        return buildResponse(
                HttpStatus.BAD_REQUEST,
                exception.getMessage()
        );
    }

    // ============================================================
    // DTO VALIDATION
    // ============================================================

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>>
    handleValidationExceptions(
            MethodArgumentNotValidException exception
    ) {
        Map<String, String> errors =
                new LinkedHashMap<>();

        exception.getBindingResult()
                .getFieldErrors()
                .forEach(error ->
                        errors.putIfAbsent(
                                error.getField(),
                                error.getDefaultMessage()
                        )
                );

        return ResponseEntity
                .badRequest()
                .body(errors);
    }

    // ============================================================
    // CONSTRAINT VALIDATION
    // ============================================================

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse>
    handleConstraintViolationException(
            ConstraintViolationException exception
    ) {
        return buildResponse(
                HttpStatus.BAD_REQUEST,
                exception.getMessage()
        );
    }

    // ============================================================
    // INVALID ENUM / PATH / REQUEST PARAMETER
    // ============================================================

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse>
    handleMethodArgumentTypeMismatch(
            MethodArgumentTypeMismatchException exception
    ) {
        String message =
                "Invalid value for parameter: "
                        + exception.getName();

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                message
        );
    }





    // ============================================================
    // DATABASE CONSTRAINT
    // ============================================================

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse>
    handleDataIntegrityViolation(
            DataIntegrityViolationException exception
    ) {
        log.error(
                "Database integrity violation",
                exception
        );

        return buildResponse(
                HttpStatus.CONFLICT,
                "The operation violates a database constraint"
        );
    }

    // ============================================================
    // UNEXPECTED ERROR
    // ============================================================

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse>
    handleGeneralException(
            Exception exception
    ) {
        /*
         * Registra el stack trace completo únicamente
         * en el servidor.
         */
        log.error(
                "Unexpected unhandled exception",
                exception
        );

        /*
         * No exponemos detalles internos al cliente.
         */
        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred"
        );
    }

    // ============================================================
    // RESPONSE BUILDER
    // ============================================================

    private ResponseEntity<ErrorResponse> buildResponse(
            HttpStatus status,
            String message
    ) {
        ErrorResponse error = new ErrorResponse(
                status.value(),
                message,
                LocalDateTime.now(ZoneId.of("Europe/Madrid"))
        );

        return ResponseEntity
                .status(status)
                .body(error);
    }




    // ============================================================
    // INVALID JSON
    // ============================================================
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex
    ) {
        String message = "Invalid or malformed request body";

        if (ex.getCause() instanceof InvalidFormatException invalidFormat
                && invalidFormat.getTargetType().isEnum()) {

            String allowedValues = Arrays.stream(
                            invalidFormat.getTargetType().getEnumConstants()
                    )
                    .map(Object::toString)
                    .collect(Collectors.joining(", "));

            message = "Invalid value '"
                    + invalidFormat.getValue()
                    + "' for "
                    + invalidFormat.getTargetType().getSimpleName()
                    + ". Allowed values: "
                    + allowedValues;
        }

        return ResponseEntity.badRequest().body(
                new ErrorResponse(
                        HttpStatus.BAD_REQUEST.value(),
                        message,
                        LocalDateTime.now(ZoneId.of("Europe/Madrid"))
                )
        );
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResourceFound(
            NoResourceFoundException exception
    ) {
        return buildResponse(
                HttpStatus.NOT_FOUND,
                "Endpoint not found"
        );
    }



    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse>
    handleHttpRequestMethodNotSupported(
            HttpRequestMethodNotSupportedException exception
    ) {
        String message =
                "HTTP method "
                        + exception.getMethod()
                        + " is not allowed for this endpoint";

        return buildResponse(
                HttpStatus.METHOD_NOT_ALLOWED,
                message
        );
    }



    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse>
    handleMissingServletRequestParameter(
            MissingServletRequestParameterException exception
    ) {
        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "Missing required parameter: "
                        + exception.getParameterName()
        );
    }




    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse>
    handleAuthenticationException(
            AuthenticationException exception
    ) {
        log.warn(
                "Authentication failed: {}",
                exception.getClass().getSimpleName()
        );

        return buildResponse(
                HttpStatus.UNAUTHORIZED,
                "Invalid email or password"
        );
    }



    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse>
    handleAccessDeniedException(
            AccessDeniedException exception
    ) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(
                        new ErrorResponse(
                                HttpStatus.FORBIDDEN.value(),
                                "Access denied",
                                LocalDateTime.now(ZoneId.of("Europe/Madrid"))
                        )
                );
    }


    // ============================================================
// LOGIN RATE LIMIT
// ============================================================

    @ExceptionHandler(LoginRateLimitException.class)
    public ResponseEntity<ErrorResponse>
    handleLoginRateLimitException(
            LoginRateLimitException exception
    ) {
        log.warn("Login temporarily blocked");

        return buildResponse(
                HttpStatus.TOO_MANY_REQUESTS,
                exception.getMessage()
        );
    }



}