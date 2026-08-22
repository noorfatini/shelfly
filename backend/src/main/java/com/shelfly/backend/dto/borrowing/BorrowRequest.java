package com.shelfly.backend.dto.borrowing;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class BorrowRequest {

    @NotBlank(message = "bookId is required")
    private String bookId;
}
