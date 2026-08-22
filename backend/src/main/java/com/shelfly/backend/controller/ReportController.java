package com.shelfly.backend.controller;

import com.shelfly.backend.dto.report.CategoryStat;
import com.shelfly.backend.dto.report.DashboardSummary;
import com.shelfly.backend.dto.report.MostBorrowedBook;
import com.shelfly.backend.dto.borrowing.BorrowingResponse;
import com.shelfly.backend.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * MongoDB aggregation / reporting endpoints. Admin-only (enforced in SecurityConfig + here).
 */
@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/summary")
    public DashboardSummary summary() {
        return reportService.dashboardSummary();
    }

    @GetMapping("/most-borrowed")
    public List<MostBorrowedBook> mostBorrowed() {
        return reportService.mostBorrowedBooks();
    }

    @GetMapping("/by-category")
    public List<CategoryStat> byCategory() {
        return reportService.borrowingsByCategory();
    }

    @GetMapping("/overdue")
    public List<BorrowingResponse> overdue() {
        return reportService.overdueBorrowings().stream()
                .map(BorrowingResponse::fromEntity)
                .toList();
    }
}
