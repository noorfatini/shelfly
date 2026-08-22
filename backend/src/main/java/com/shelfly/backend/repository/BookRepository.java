package com.shelfly.backend.repository;

import com.shelfly.backend.model.Book;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface BookRepository extends MongoRepository<Book, String>, BookRepositoryCustom {
    boolean existsByIsbn(String isbn);
}
