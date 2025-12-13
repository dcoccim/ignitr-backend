package dev.ignitr.ignitrbackend.reason.exception;

import lombok.Getter;

@Getter
public class ReasonAlreadyExistsException extends RuntimeException {

    private final String content;

    public ReasonAlreadyExistsException(String content) {
        super("Reason with content '%s' already exists".formatted(content));
        this.content = content;
    }
}
