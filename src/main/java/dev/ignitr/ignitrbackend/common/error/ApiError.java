package dev.ignitr.ignitrbackend.common.error;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.time.Instant;

@Getter
public class ApiError {

    private final Instant timestamp = Instant.now();

    private final int status;

    private final String error;

    private final String code;

    private final String message;

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
        String message = (messageOverride != null && !messageOverride.isBlank())
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
