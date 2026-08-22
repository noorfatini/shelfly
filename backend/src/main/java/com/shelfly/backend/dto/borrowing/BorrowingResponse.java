package com.shelfly.backend.dto.borrowing;

import com.shelfly.backend.model.Borrowing;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BorrowingResponse {
    private String id;
    private String bookId;
    private String bookTitle;
    private String bookCategory;
    private String userId;
    private String userName;
    private Instant borrowDate;
    private Instant dueDate;
    private Instant returnDate;
    private String status;

    public static BorrowingResponse fromEntity(Borrowing b) {
        return BorrowingResponse.builder()
                .id(b.getId())
                .bookId(b.getBookId())
                .bookTitle(b.getBookTitle())
                .bookCategory(b.getBookCategory())
                .userId(b.getUserId())
                .userName(b.getUserName())
                .borrowDate(b.getBorrowDate())
                .dueDate(b.getDueDate())
                .returnDate(b.getReturnDate())
                .status(b.getStatus().name())
                .build();
    }
}
