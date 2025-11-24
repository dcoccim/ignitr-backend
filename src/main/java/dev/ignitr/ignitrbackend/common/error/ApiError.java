package dev.ignitr.ignitrbackend.common.error;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.time.Instant;

@Getter
@Schema(name = "ErrorResponse", description = "Standard format for API errors")
public class ApiError {

    private final Instant timestamp = Instant.now();

    @Schema(description = "HTTP status code", example = "409")
    private final int status;

    @Schema(description = "Reason text", example = "Conflict")
    private final String error;

    @Schema(description = "Machine-readable error code", example = "SPARK_ALREADY_EXISTS")
    private final String code;

    @Schema(description = "Human-readable error message", example = "A Spark with title 'X' already exists")
    private final String message;

    @Schema(description = "Request path", example = "/api/sparks")
    private final String path;

    public ApiError(int status, String error, String code, String message, String path) {
        this.status = status;
        this.error = error;
        this.code = code;
        this.message = message;
        this.path = path;
    }

    public static ApiError fromCode(ApiErrorCode errorCode, String messageOverride, String path) {
        HttpStatus status = errorCode.getHttpStatus();
        String message = messageOverride != null && messageOverride.isBlank()
                ? messageOverride
                : errorCode.getDefaultMessage();

        return new ApiError(
                status.value(),
                status.getReasonPhrase(),
                errorCode.getCode(),
                message,
                path
        );
    }
}
