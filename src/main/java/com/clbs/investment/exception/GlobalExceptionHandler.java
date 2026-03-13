package com.clbs.investment.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Centralised error handling — maps to COBOL ERRHNDL (online) and ERRPROC (batch).
 * Returns structured error responses instead of COBOL error screens.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(TransactionValidationException.class)
    public ResponseEntity<Map<String, Object>> handleValidationError(TransactionValidationException ex) {
        log.error("Transaction validation failed [{}] transId={}: {}", ex.getErrorCode(), ex.getTransId(), ex.getMessage());
        return ResponseEntity.badRequest().body(errorBody(ex.getErrorCode(), ex.getMessage()));
    }

    @ExceptionHandler(PositionUpdateException.class)
    public ResponseEntity<Map<String, Object>> handlePositionUpdateError(PositionUpdateException ex) {
        log.error("Position update failed account={} fund={}: {}", ex.getAccountNo(), ex.getFundId(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorBody("E009", ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleBeanValidation(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(fe.getField(), fe.getDefaultMessage());
        }
        Map<String, Object> body = errorBody("E400", "Validation failed");
        body.put("fieldErrors", fieldErrors);
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneral(Exception ex) {
        log.error("Unexpected error", ex);
        return ResponseEntity.internalServerError().body(errorBody("E500", "Internal server error"));
    }

    private Map<String, Object> errorBody(String code, String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("errorCode", code);
        body.put("message", message);
        body.put("timestamp", LocalDateTime.now().toString());
        return body;
    }
}
