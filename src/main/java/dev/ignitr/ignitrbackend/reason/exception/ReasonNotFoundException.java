package dev.ignitr.ignitrbackend.reason.exception;

import lombok.Getter;

@Getter
public class ReasonNotFoundException extends RuntimeException {

    private final String id;

    public ReasonNotFoundException(String id) {
        super("Reason with id '%s' was not found".formatted(id));
        this.id = id;
    }
}
