package dev.ignitr.ignitrbackend.common.error;

import dev.ignitr.ignitrbackend.reason.exception.ReasonAlreadyExistsException;
import dev.ignitr.ignitrbackend.reason.exception.ReasonNotFoundException;
import dev.ignitr.ignitrbackend.spark.exception.SparkAlreadyExistsException;
import dev.ignitr.ignitrbackend.spark.exception.SparkNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.HashMap;
import java.util.Map;

import static dev.ignitr.ignitrbackend.common.error.ApiErrorCode.*;
import static dev.ignitr.ignitrbackend.common.utils.LoggingUtils.error;
import static dev.ignitr.ignitrbackend.common.utils.LoggingUtils.warn;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(SparkAlreadyExistsException.class)
    public ResponseEntity<ApiError> handleSparkAlreadyExists(SparkAlreadyExistsException ex, WebRequest request) {

        warn(logger, "handleSparkAlreadyExists", null, "Spark already exists: {}", ex.getMessage());

        ApiError errorBody = ApiError.fromCode(SPARK_ALREADY_EXISTS, ex.getMessage(), request.getDescription(false));

        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorBody);
    }

    @ExceptionHandler(ReasonAlreadyExistsException.class)
    public ResponseEntity<ApiError> handleReasonAlreadyExists(ReasonAlreadyExistsException ex, WebRequest request) {

        warn(logger, "handleReasonAlreadyExists", null, "Reason already exists: {}", ex.getMessage());

        ApiError errorBody = ApiError.fromCode(REASON_ALREADY_EXISTS, ex.getMessage(), request.getDescription(false));

        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorBody);
    }

    @ExceptionHandler(SparkNotFoundException.class)
    public ResponseEntity<ApiError> handleSparkNotFound(SparkNotFoundException ex, WebRequest request) {
        warn(logger, "handleSparkNotFound", null, "Spark not found: {}", ex.getMessage());
        ApiError errorBody = ApiError.fromCode(SPARK_NOT_FOUND, ex.getMessage(), request.getDescription(false));
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorBody);
    }

    @ExceptionHandler(ReasonNotFoundException.class)
    public ResponseEntity<ApiError> handleReasonNotFound(ReasonNotFoundException ex, WebRequest request) {
        warn(logger, "handleReasonNotFound", null, "Reason not found: {}", ex.getMessage());
        ApiError errorBody = ApiError.fromCode(REASON_NOT_FOUND, ex.getMessage(), request.getDescription(false));
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorBody);
    }

    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<ApiError> handleDuplicateKey(DuplicateKeyException ex, WebRequest request) {
        warn(logger, "handleDuplicateKey", null, "Duplicate key error: {}", ex.getMessage());
        ApiError errorBody = ApiError.fromCode(DUPLICATE_KEY, ex.getMessage(), request.getDescription(false));
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorBody);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidationExceptions(MethodArgumentNotValidException ex, WebRequest request) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        ApiError errorBody = ApiError.fromCode(VALIDATION_ERROR, errors.toString(), request.getDescription(false));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorBody);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleAll(Exception ex, WebRequest request) {
        error(
                logger,
                "handleGeneric",
                null, ex,
                "Internal server error at path=[{}]", request.getDescription(false));
        ApiError errorBody = ApiError.fromCode(INTERNAL_ERROR, ex.getMessage(), request.getDescription(false));
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorBody);
    }
}
