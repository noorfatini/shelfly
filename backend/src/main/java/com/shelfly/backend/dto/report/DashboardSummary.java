package com.shelfly.backend.dto.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardSummary {
    private long totalBooks;
    private long totalCopies;
    private long totalAvailableCopies;
    private long activeBorrowings;
    private long overdueBorrowings;
    private long totalMembers;
}
