package com.shelfly.backend.service;

import com.shelfly.backend.dto.report.CategoryStat;
import com.shelfly.backend.dto.report.DashboardSummary;
import com.shelfly.backend.dto.report.MostBorrowedBook;
import com.shelfly.backend.model.Book;
import com.shelfly.backend.model.Borrowing;
import com.shelfly.backend.model.BorrowingStatus;
import com.shelfly.backend.repository.BookRepository;
import com.shelfly.backend.repository.BorrowingRepository;
import com.shelfly.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.SortOperation;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * MongoDB aggregation / reporting features required by the capstone brief.
 * These use the native aggregation pipeline (group + sort + limit) rather than
 * pulling everything into Java and counting in memory.
 */
@Service
@RequiredArgsConstructor
public class ReportService {

    private final MongoTemplate mongoTemplate;
    private final BookRepository bookRepository;
    private final BorrowingRepository borrowingRepository;
    private final UserRepository userRepository;

    /** Most borrowed books: group borrowings by bookId, count, sort desc, top 5. */
    public List<MostBorrowedBook> mostBorrowedBooks() {
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.group("bookId")
                        .first("bookTitle").as("title")
                        .count().as("borrowCount"),
                Aggregation.sort(Sort.Direction.DESC, "borrowCount"),
                Aggregation.limit(5)
        );

        AggregationResults<MostBorrowedBook> results =
                mongoTemplate.aggregate(aggregation, "borrowings", MostBorrowedBook.class);
        return results.getMappedResults();
    }

    /** Borrowings grouped by book category, most borrowed category first. */
    public List<CategoryStat> borrowingsByCategory() {
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.group("bookCategory").count().as("borrowCount"),
                Aggregation.sort(Sort.Direction.DESC, "borrowCount")
        );

        AggregationResults<CategoryStat> results =
                mongoTemplate.aggregate(aggregation, "borrowings", CategoryStat.class);
        return results.getMappedResults();
    }

    public List<Borrowing> overdueBorrowings() {
        return borrowingRepository.findByStatusAndDueDateBefore(BorrowingStatus.BORROWED, java.time.Instant.now());
    }

    public DashboardSummary dashboardSummary() {
        long totalBooks = mongoTemplate.count(new org.springframework.data.mongodb.core.query.Query(), Book.class);

        // Sum totalCopies / availableCopies across the collection via aggregation instead of pulling docs into Java.
        Aggregation sumAgg = Aggregation.newAggregation(
                Aggregation.group()
                        .sum("totalCopies").as("totalCopies")
                        .sum("availableCopies").as("totalAvailableCopies")
        );
        AggregationResults<java.util.Map> sums = mongoTemplate.aggregate(sumAgg, "books", java.util.Map.class);
        java.util.Map<String, Object> sumsResult = sums.getUniqueMappedResult();
        long totalCopies = sumsResult == null ? 0 : ((Number) sumsResult.getOrDefault("totalCopies", 0)).longValue();
        long totalAvailable = sumsResult == null ? 0 : ((Number) sumsResult.getOrDefault("totalAvailableCopies", 0)).longValue();

        long activeBorrowings = mongoTemplate.count(
                org.springframework.data.mongodb.core.query.Query.query(
                        org.springframework.data.mongodb.core.query.Criteria.where("status").is(BorrowingStatus.BORROWED)),
                Borrowing.class);

        long overdue = borrowingRepository.findByStatusAndDueDateBefore(
                BorrowingStatus.BORROWED, java.time.Instant.now()).size();

        long totalMembers = mongoTemplate.count(
                org.springframework.data.mongodb.core.query.Query.query(
                        org.springframework.data.mongodb.core.query.Criteria.where("role").is(com.shelfly.backend.model.Role.MEMBER)),
                com.shelfly.backend.model.User.class);

        return DashboardSummary.builder()
                .totalBooks(totalBooks)
                .totalCopies(totalCopies)
                .totalAvailableCopies(totalAvailable)
                .activeBorrowings(activeBorrowings)
                .overdueBorrowings(overdue)
                .totalMembers(totalMembers)
                .build();
    }
}
