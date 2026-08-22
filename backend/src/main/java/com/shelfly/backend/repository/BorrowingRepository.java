package com.shelfly.backend.repository;

import com.shelfly.backend.model.Borrowing;
import com.shelfly.backend.model.BorrowingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface BorrowingRepository extends MongoRepository<Borrowing, String> {

    Page<Borrowing> findByUserId(String userId, Pageable pageable);

    Page<Borrowing> findByStatus(BorrowingStatus status, Pageable pageable);

    List<Borrowing> findByUserIdAndBookIdAndStatus(String userId, String bookId, BorrowingStatus status);

    long countByUserIdAndStatus(String userId, BorrowingStatus status);

    Optional<Borrowing> findByIdAndUserId(String id, String userId);

    List<Borrowing> findByStatusAndDueDateBefore(BorrowingStatus status, java.time.Instant instant);
}
