package com.shelfly.backend.controller;

import com.shelfly.backend.dto.PageResponse;
import com.shelfly.backend.dto.book.BookRequest;
import com.shelfly.backend.dto.book.BookResponse;
import com.shelfly.backend.model.Book;
import com.shelfly.backend.service.BookService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Main entity management: /api/books
 * GET endpoints are public (anyone can browse the catalogue).
 * Write endpoints are restricted to ADMIN in SecurityConfig.
 */
@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    @GetMapping
    public ResponseEntity<PageResponse<BookResponse>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "title") String sortBy,
            @RequestParam(defaultValue = "asc") String direction
    ) {
        Sort sort = direction.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Page<Book> result = bookService.search(keyword, category, status, PageRequest.of(page, size, sort));
        Page<BookResponse> mapped = result.map(BookResponse::fromEntity);
        return ResponseEntity.ok(PageResponse.from(mapped));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookResponse> getById(@PathVariable String id) {
        return ResponseEntity.ok(BookResponse.fromEntity(bookService.getById(id)));
    }

    @PostMapping
    public ResponseEntity<BookResponse> create(@Valid @RequestBody BookRequest request) {
        Book created = bookService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(BookResponse.fromEntity(created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BookResponse> update(@PathVariable String id, @Valid @RequestBody BookRequest request) {
        return ResponseEntity.ok(BookResponse.fromEntity(bookService.update(id, request)));
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivate(@PathVariable String id) {
        bookService.deactivate(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        bookService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
