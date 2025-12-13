package dev.ignitr.ignitrbackend.common.error;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ApiErrorCode {

    SPARK_ALREADY_EXISTS(HttpStatus.CONFLICT, "SPARK_ALREADY_EXISTS", "A spark with this title already exists."),
    SPARK_NOT_FOUND(HttpStatus.NOT_FOUND, "SPARK_NOT_FOUND", "Spark not found"),
    REASON_ALREADY_EXISTS(HttpStatus.CONFLICT, "REASON_ALREADY_EXISTS", "A reason with this content already exists."),
    REASON_NOT_FOUND(HttpStatus.NOT_FOUND, "REASON_NOT_FOUND", "Reason not found"),
    DUPLICATE_KEY(HttpStatus.CONFLICT, "DUPLICATE_KEY", "A resource with the same unique field already exists."),
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Request validation failed."),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "An unexpected error occurred.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String defaultMessage;

    ApiErrorCode(HttpStatus httpStatus, String code, String defaultMessage) {
       this.httpStatus = httpStatus;
       this.code = code;
       this.defaultMessage = defaultMessage;
    }

}
