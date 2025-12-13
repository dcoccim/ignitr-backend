package dev.ignitr.ignitrbackend.reason.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ReasonType {
    GOOD("good"),
    BAD("bad");

    private final String value;

    ReasonType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static ReasonType fromValue(String value) {
        for (ReasonType type : ReasonType.values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown reason type: '" + value + "'. Valid values are: 'good', 'bad'");
    }
}
