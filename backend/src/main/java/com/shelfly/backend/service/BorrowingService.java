package com.shelfly.backend.service;

import com.shelfly.backend.exception.BusinessRuleException;
import com.shelfly.backend.exception.ResourceNotFoundException;
import com.shelfly.backend.model.*;
import com.shelfly.backend.repository.BookRepository;
import com.shelfly.backend.repository.BorrowingRepository;
import com.shelfly.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BorrowingService {

    private final BorrowingRepository borrowingRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;

    @Value("${SHELFLY_MAX_ACTIVE_BORROWINGS}")
    private int maxActiveBorrowings;

    @Value("${SHELFLY_LOAN_PERIOD_DAYS}")
    private int loanPeriodDays;

    public Borrowing borrow(String userId, String bookId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found"));

        // Business rule: inactive books cannot be borrowed
        if (book.getStatus() != BookStatus.ACTIVE) {
            throw new BusinessRuleException("This book is not currently available for borrowing");
        }

        // Business rule: must have available copies
        if (book.getAvailableCopies() <= 0) {
            throw new BusinessRuleException("No copies of this book are currently available");
        }

        // Business rule: cannot borrow the same book twice while still holding a copy
        List<Borrowing> existing = borrowingRepository.findByUserIdAndBookIdAndStatus(userId, bookId, BorrowingStatus.BORROWED);
        if (!existing.isEmpty()) {
            throw new BusinessRuleException("You already have an active borrowing for this book");
        }

        // Business rule: cannot exceed max active borrowings
        long activeCount = borrowingRepository.countByUserIdAndStatus(userId, BorrowingStatus.BORROWED);
        if (activeCount >= maxActiveBorrowings) {
            throw new BusinessRuleException("You have reached the maximum of " + maxActiveBorrowings + " active borrowings");
        }

        // All checks passed -> decrement availability and create the borrowing record
        book.setAvailableCopies(book.getAvailableCopies() - 1);
        bookRepository.save(book);

        Instant now = Instant.now();
        Borrowing borrowing = Borrowing.builder()
                .userId(userId)
                .bookId(bookId)
                .bookTitle(book.getTitle())
                .bookCategory(book.getCategory())
                .userName(user.getName())
                .borrowDate(now)
                .dueDate(now.plus(loanPeriodDays, ChronoUnit.DAYS))
                .status(BorrowingStatus.BORROWED)
                .build();

        return borrowingRepository.save(borrowing);
    }

    public Borrowing returnBook(String borrowingId, String userId, boolean isAdmin) {
        Borrowing borrowing = borrowingRepository.findById(borrowingId)
                .orElseThrow(() -> new ResourceNotFoundException("Borrowing record not found"));

        // Members can only return their own borrowings; admins can return any
        if (!isAdmin && !borrowing.getUserId().equals(userId)) {
            throw new BusinessRuleException("You can only return your own borrowings");
        }

        if (borrowing.getStatus() == BorrowingStatus.RETURNED) {
            throw new BusinessRuleException("This book has already been returned");
        }

        borrowing.setStatus(BorrowingStatus.RETURNED);
        borrowing.setReturnDate(Instant.now());
        borrowingRepository.save(borrowing);

        // Returning a book updates the related book's availability
        Book book = bookRepository.findById(borrowing.getBookId())
                .orElseThrow(() -> new ResourceNotFoundException("Book not found"));
        book.setAvailableCopies(Math.min(book.getTotalCopies(), book.getAvailableCopies() + 1));
        bookRepository.save(book);

        return borrowing;
    }

    public Page<Borrowing> myBorrowings(String userId, Pageable pageable) {
        return borrowingRepository.findByUserId(userId, pageable);
    }

    public Page<Borrowing> allBorrowings(String statusFilter, Pageable pageable) {
        if (statusFilter != null && !statusFilter.isBlank()) {
            return borrowingRepository.findByStatus(BorrowingStatus.valueOf(statusFilter.toUpperCase()), pageable);
        }
        return borrowingRepository.findAll(pageable);
    }

    /**
     * Cancelled/overdue transitions should update related records automatically.
     * Runs hourly, and also opportunistically before reports, to flip BORROWED -> OVERDUE
     * once the due date has passed.
     */
    @Scheduled(fixedRate = 60 * 60 * 1000)
    public void markOverdueBorrowings() {
        List<Borrowing> overdue = borrowingRepository.findByStatusAndDueDateBefore(BorrowingStatus.BORROWED, Instant.now());
        for (Borrowing b : overdue) {
            b.setStatus(BorrowingStatus.OVERDUE);
        }
        if (!overdue.isEmpty()) {
            borrowingRepository.saveAll(overdue);
        }
    }
}
