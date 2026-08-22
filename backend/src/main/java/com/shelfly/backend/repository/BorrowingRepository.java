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

    List<Borrowing> findByUserIdAndBookIdAndStatusIn(String userId, String bookId, List<BorrowingStatus> statuses);

    long countByUserIdAndStatusIn(String userId, List<BorrowingStatus> statuses);

    Optional<Borrowing> findByIdAndUserId(String id, String userId);

    List<Borrowing> findByStatusAndDueDateBefore(BorrowingStatus status, java.time.Instant instant);

    List<Borrowing> findByStatusInAndDueDateBefore(List<BorrowingStatus> statuses, java.time.Instant instant);
}
