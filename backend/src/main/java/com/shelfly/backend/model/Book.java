package com.shelfly.backend.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "books")
public class Book {

    @Id
    private String id;

    @Indexed
    private String title;

    private String author;

    @Indexed(unique = true, sparse = true)
    private String isbn;

    @Indexed
    private String category;

    private String description;

    private int totalCopies;

    private int availableCopies;

    @Builder.Default
    @Indexed
    private BookStatus status = BookStatus.ACTIVE;

    @Builder.Default
    private Instant createdAt = Instant.now();
}
