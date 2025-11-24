package dev.ignitr.ignitrbackend.spark.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Getter
@Setter
@Document(collection = "sparks")
public class Spark {

    @Id
    private String id;

    @Indexed(unique = true)
    private String title;

    private String description;

    private String parentId;

    private Instant createdAt;

    private Instant updatedAt;

    public Spark() {}

    public Spark(String id, String title, String description, String parentId, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.parentId = parentId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

}
