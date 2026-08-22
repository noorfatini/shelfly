package com.shelfly.backend.dto.book;

import com.shelfly.backend.model.Book;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookResponse {
    private String id;
    private String title;
    private String author;
    private String isbn;
    private String category;
    private String description;
    private int totalCopies;
    private int availableCopies;
    private String status;
    private Instant createdAt;

    public static BookResponse fromEntity(Book book) {
        return BookResponse.builder()
                .id(book.getId())
                .title(book.getTitle())
                .author(book.getAuthor())
                .isbn(book.getIsbn())
                .category(book.getCategory())
                .description(book.getDescription())
                .totalCopies(book.getTotalCopies())
                .availableCopies(book.getAvailableCopies())
                .status(book.getStatus().name())
                .createdAt(book.getCreatedAt())
                .build();
    }
}
