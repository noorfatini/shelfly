package com.shelfly.backend.dto.book;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class BookRequest {

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Author is required")
    private String author;

    private String isbn;

    @NotBlank(message = "Category is required")
    private String category;

    private String description;

    @Min(value = 1, message = "Total copies must be at least 1")
    private int totalCopies;

    private String status; // ACTIVE / INACTIVE - optional on create, defaults to ACTIVE
}
