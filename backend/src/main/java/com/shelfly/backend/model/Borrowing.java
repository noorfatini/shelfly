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
@Document(collection = "borrowings")
public class Borrowing {

    @Id
    private String id;

    @Indexed
    private String userId;

    @Indexed
    private String bookId;

    // Denormalised snapshot fields so reports/lists don't need extra lookups every time
    private String bookTitle;
    private String bookCategory;
    private String userName;

    private Instant borrowDate;

    private Instant dueDate;

    private Instant returnDate;

    @Builder.Default
    @Indexed
    private BorrowingStatus status = BorrowingStatus.BORROWED;

    @Builder.Default
    private Instant createdAt = Instant.now();
}
