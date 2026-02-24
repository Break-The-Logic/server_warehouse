package com.greateastern.warehouse.common.error;

import com.greateastern.warehouse.common.api.ApiResponse;
import com.greateastern.warehouse.common.api.ApiResponses;
import com.greateastern.warehouse.common.api.ApiMessage;
import com.greateastern.warehouse.common.api.BugFailureMessage;
import com.greateastern.warehouse.common.api.ExternalApiFailureMessage;
import com.greateastern.warehouse.common.api.MissingFieldsFailureMessage;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<ApiResponse<String>> handleNotFound(ResourceNotFoundException ex) {
    return buildFailureResponse(
        HttpStatus.NOT_FOUND,
        new BugFailureMessage("Resource not found", safeReason(ex.getMessage()), "Verify identifier and retry")
    );
  }

  @ExceptionHandler(ConflictException.class)
  public ResponseEntity<ApiResponse<String>> handleConflict(ConflictException ex) {
    return buildFailureResponse(
        HttpStatus.CONFLICT,
        new BugFailureMessage("Request conflict", safeReason(ex.getMessage()), "Adjust request data to avoid duplication")
    );
  }

  @ExceptionHandler(BusinessRuleException.class)
  public ResponseEntity<ApiResponse<String>> handleBusinessRule(BusinessRuleException ex) {
    return buildFailureResponse(
        HttpStatus.UNPROCESSABLE_ENTITY,
        new BugFailureMessage("Business rule violation", safeReason(ex.getMessage()), "Update business input and retry")
    );
  }

  @ExceptionHandler(ThirdPartyApiException.class)
  public ResponseEntity<ApiResponse<String>> handleThirdParty(ThirdPartyApiException ex) {
    return buildFailureResponse(
        HttpStatus.BAD_GATEWAY,
        new ExternalApiFailureMessage(safeReason(ex.getOriginalCode()), safeReason(ex.getOriginalMessage()))
    );
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiResponse<String>> handleValidation(MethodArgumentNotValidException ex) {
    Map<String, String> expectedFields = new LinkedHashMap<>();

    for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
      expectedFields.put(fieldError.getField(), resolveExpectedValue(fieldError.getCode(), fieldError.getDefaultMessage()));
    }

    if (expectedFields.isEmpty()) {
      expectedFields.put("request", "valid request body");
    }

    return buildFailureResponse(HttpStatus.BAD_REQUEST, new MissingFieldsFailureMessage(expectedFields));
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<ApiResponse<String>> handleConstraintViolation(ConstraintViolationException ex) {
    Map<String, String> expectedFields = new LinkedHashMap<>();

    for (ConstraintViolation<?> violation : ex.getConstraintViolations()) {
      String path = violation.getPropertyPath().toString();
      String fieldName = resolveFieldName(path);
      String constraintCode = violation.getConstraintDescriptor().getAnnotation().annotationType().getSimpleName();
      expectedFields.put(fieldName, resolveExpectedValue(constraintCode, violation.getMessage()));
    }

    if (expectedFields.isEmpty()) {
      expectedFields.put("request", "valid request parameters");
    }

    return buildFailureResponse(HttpStatus.BAD_REQUEST, new MissingFieldsFailureMessage(expectedFields));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiResponse<String>> handleUnexpected(Exception ex) {
    String failureReason = safeReason(ex.getClass().getSimpleName() + ": " + ex.getMessage());
    return buildFailureResponse(
        HttpStatus.INTERNAL_SERVER_ERROR,
        new BugFailureMessage("Unhandled exception", failureReason, "Inspect server logs and fix the underlying issue")
    );
  }

  private ResponseEntity<ApiResponse<String>> buildFailureResponse(HttpStatus status, ApiMessage failureMessage) {
    return ResponseEntity.status(status).body(ApiResponses.failure(failureMessage));
  }

  private String resolveExpectedValue(String validationCode, String defaultMessage) {
    if ("NotBlank".equals(validationCode)) {
      return "non-empty string";
    }

    if ("NotNull".equals(validationCode)) {
      return "required non-null value";
    }

    if ("NotEmpty".equals(validationCode)) {
      return "non-empty collection";
    }

    if ("Min".equals(validationCode)) {
      return "integer value greater than or equal to the declared minimum";
    }

    if ("DecimalMin".equals(validationCode)) {
      return "decimal value greater than the declared minimum";
    }

    if ("Size".equals(validationCode)) {
      return "string length within the declared limit";
    }

    return safeReason(defaultMessage);
  }

  private String resolveFieldName(String path) {
    if (path == null || path.isBlank()) {
      return "request";
    }

    String[] segments = path.split("\\.");

    if (segments.length == 0) {
      return "request";
    }

    return segments[segments.length - 1];
  }

  private String safeReason(String rawReason) {
    if (rawReason == null || rawReason.isBlank()) {
      return "No details provided";
    }

    return rawReason;
  }
}
