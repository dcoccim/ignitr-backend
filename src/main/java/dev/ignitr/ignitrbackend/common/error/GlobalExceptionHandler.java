package dev.ignitr.ignitrbackend.common.error;

import dev.ignitr.ignitrbackend.spark.exception.SparkAlreadyExistsException;
import dev.ignitr.ignitrbackend.spark.exception.SparkNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(SparkAlreadyExistsException.class)
    public ResponseEntity<ApiError> handleSparkAlreadyExists(
            SparkAlreadyExistsException ex,
            HttpServletRequest request
    ) {

        String uri = request.getRequestURI();

        log.warn("SparkAlreadyExistsException: title='{}', path={}", ex.getTitle(), uri);

        ApiError error = ApiError.fromCode(
                ApiErrorCode.SPARK_ALREADY_EXISTS,
                ex.getMessage(),
                uri
        );

        return ResponseEntity.status(ApiErrorCode.SPARK_ALREADY_EXISTS.getHttpStatus()).body(error);
    }

    @ExceptionHandler(SparkNotFoundException.class)
    public ResponseEntity<ApiError> handleSparkNotFound(
            SparkNotFoundException ex,
            HttpServletRequest request
    ) {

        String uri = request.getRequestURI();

        log.warn("SparkNotFoundException: id='{}', path={}", ex.getId(), uri);

        ApiError error = ApiError.fromCode(
                ApiErrorCode.SPARK_NOT_FOUND,
                ex.getMessage(),
                uri
        );

        return ResponseEntity
                .status(ApiErrorCode.SPARK_NOT_FOUND.getHttpStatus())
                .body(error);
    }

    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<ApiError> handleDuplicateKey(
            DuplicateKeyException ex,
            HttpServletRequest request
    ) {

        String uri = request.getRequestURI();

        log.warn("DuplicateKeyException at path = {}: {}", uri, ex.getMessage());

        ApiError error = ApiError.fromCode(
                ApiErrorCode.DUPLICATE_KEY,
                null,
                uri
        );

        return ResponseEntity.status(ApiErrorCode.DUPLICATE_KEY.getHttpStatus()).body(error);
    }

    private String formatFieldError(FieldError fieldError) {
        return "%s %s".formatted(fieldError.getField(), fieldError.getDefaultMessage());
    }

    // Validation errors
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {

        String uri = request.getRequestURI();

        String fieldMessages = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(this::formatFieldError)
                .collect(Collectors.joining("; "));

        log.warn("Validation error at path={}: {}", uri, fieldMessages);

        ApiError error = ApiError.fromCode(
                ApiErrorCode.VALIDATION_ERROR,
                fieldMessages,
                uri
        );

        return ResponseEntity.status(ApiErrorCode.VALIDATION_ERROR.getHttpStatus()).body(error);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex,
            HttpServletRequest request
    ) {
        String uri = request.getRequestURI();
        String name = ex.getName();
        String value = String.valueOf(ex.getValue());

        log.warn("Type mismatch for parameter '{}' with value '{}' at path={}",
                name, value, uri);

        ApiError error = ApiError.fromCode(
                ApiErrorCode.VALIDATION_ERROR,
                "Invalid value for parameter '%s': '%s'".formatted(name, value),
                uri
        );

        return ResponseEntity.status(ApiErrorCode.VALIDATION_ERROR.getHttpStatus())
                .body(error);
    }

    // Catch-all
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneric(
            Exception ex,
            HttpServletRequest request
    ) {

        String uri = request.getRequestURI();

        log.error("Unexpected error at path={}", uri, ex);

        ApiError error = ApiError.fromCode(
                ApiErrorCode.INTERNAL_ERROR,
                null,
                uri
        );

        return ResponseEntity.status(ApiErrorCode.INTERNAL_ERROR.getHttpStatus()).body(error);
    }

}
