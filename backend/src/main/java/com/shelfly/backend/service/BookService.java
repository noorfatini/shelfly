package com.shelfly.backend.service;

import com.shelfly.backend.dto.book.BookRequest;
import com.shelfly.backend.dto.book.BookResponse;
import com.shelfly.backend.exception.BusinessRuleException;
import com.shelfly.backend.exception.ResourceNotFoundException;
import com.shelfly.backend.model.Book;
import com.shelfly.backend.model.BookStatus;
import com.shelfly.backend.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;

    public Page<Book> search(String keyword, String category, String status, Pageable pageable) {
        return bookRepository.search(keyword, category, status, pageable);
    }

    public Book getById(String id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found: " + id));
    }

    public Book create(BookRequest request) {
        if (request.getIsbn() != null && !request.getIsbn().isBlank()
                && bookRepository.existsByIsbn(request.getIsbn())) {
            throw new BusinessRuleException("A book with this ISBN already exists");
        }

        Book book = Book.builder()
                .title(request.getTitle())
                .author(request.getAuthor())
                .isbn(request.getIsbn())
                .category(request.getCategory())
                .description(request.getDescription())
                .totalCopies(request.getTotalCopies())
                .availableCopies(request.getTotalCopies())
                .status(parseStatus(request.getStatus()))
                .build();

        return bookRepository.save(book);
    }

    public Book update(String id, BookRequest request) {
        Book book = getById(id);

        int borrowedCopies = book.getTotalCopies() - book.getAvailableCopies();
        if (request.getTotalCopies() < borrowedCopies) {
            throw new BusinessRuleException(
                    "Total copies cannot be less than copies currently on loan (" + borrowedCopies + ")");
        }

        book.setTitle(request.getTitle());
        book.setAuthor(request.getAuthor());
        book.setIsbn(request.getIsbn());
        book.setCategory(request.getCategory());
        book.setDescription(request.getDescription());
        // Keep availableCopies consistent when total changes
        int delta = request.getTotalCopies() - book.getTotalCopies();
        book.setTotalCopies(request.getTotalCopies());
        book.setAvailableCopies(book.getAvailableCopies() + delta);
        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            book.setStatus(parseStatus(request.getStatus()));
        }

        return bookRepository.save(book);
    }

    public void deactivate(String id) {
        Book book = getById(id);
        book.setStatus(BookStatus.INACTIVE);
        bookRepository.save(book);
    }

    public void delete(String id) {
        Book book = getById(id);
        int borrowedCopies = book.getTotalCopies() - book.getAvailableCopies();
        if (borrowedCopies > 0) {
            throw new BusinessRuleException("Cannot delete a book that still has copies on loan. Deactivate it instead.");
        }
        bookRepository.deleteById(id);
    }

    private BookStatus parseStatus(String status) {
        if (status == null || status.isBlank()) return BookStatus.ACTIVE;
        try {
            return BookStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid status: " + status);
        }
    }
}
