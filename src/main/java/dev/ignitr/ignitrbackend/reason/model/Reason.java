package dev.ignitr.ignitrbackend.reason.model;

import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.TypeAlias;

import java.time.Instant;

@Getter
@Setter
@TypeAlias("Reason")
public class Reason {

    private ObjectId id;
    private ReasonType type;
    private String content;
    private Integer votes = 1;
    private Instant createdAt;
    private Instant updatedAt;

    public Reason() {
        this.id = new ObjectId();
    }

    public Reason(ReasonType type, String content, Instant createdAt, Instant updatedAt) {
        this.id = new ObjectId();
        this.type = type;
        this.content = content;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Reason(ObjectId id, ReasonType type, String content, Instant createdAt, Instant updatedAt) {
        this.id = id != null ? id : new ObjectId();
        this.type = type;
        this.content = content;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

}
