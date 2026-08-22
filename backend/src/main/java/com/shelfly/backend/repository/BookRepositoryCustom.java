package com.shelfly.backend.repository;

import com.shelfly.backend.model.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Custom query logic for search + filter + sort + pagination on the book catalogue.
 * Implemented manually (instead of Spring Data derived query) because we combine
 * an optional free-text keyword search with an optional category filter at the same time.
 */
public interface BookRepositoryCustom {
    Page<Book> search(String keyword, String category, String status, Pageable pageable);
}
