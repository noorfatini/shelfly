package com.shelfly.backend.controller;

import com.shelfly.backend.dto.PageResponse;
import com.shelfly.backend.dto.borrowing.BorrowRequest;
import com.shelfly.backend.dto.borrowing.BorrowingResponse;
import com.shelfly.backend.model.Borrowing;
import com.shelfly.backend.service.BorrowingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

/**
 * Action / transaction entity management: /api/borrowings
 */
@RestController
@RequestMapping("/api/borrowings")
@RequiredArgsConstructor
public class BorrowingController {

    private final BorrowingService borrowingService;

    @PostMapping
    public ResponseEntity<BorrowingResponse> borrow(@Valid @RequestBody BorrowRequest request, Authentication auth) {
        Borrowing borrowing = borrowingService.borrow(currentUserId(auth), request.getBookId());
        return ResponseEntity.status(HttpStatus.CREATED).body(BorrowingResponse.fromEntity(borrowing));
    }

    @GetMapping("/my")
    public ResponseEntity<PageResponse<BorrowingResponse>> myBorrowings(
            Authentication auth,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<Borrowing> result = borrowingService.myBorrowings(
                currentUserId(auth), PageRequest.of(page, size, Sort.by("createdAt").descending()));
        return ResponseEntity.ok(PageResponse.from(result.map(BorrowingResponse::fromEntity)));
    }

    @PatchMapping("/{id}/return")
    public ResponseEntity<BorrowingResponse> returnBook(@PathVariable String id, Authentication auth) {
        Borrowing borrowing = borrowingService.returnBook(id, currentUserId(auth), isAdmin(auth));
        return ResponseEntity.ok(BorrowingResponse.fromEntity(borrowing));
    }

    /** Admin-only: view all borrowing/transaction records. Enforced in SecurityConfig via /api/admin/** — exposed here under /api/borrowings/all for convenience, guarded below. */
    @GetMapping("/all")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PageResponse<BorrowingResponse>> allBorrowings(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<Borrowing> result = borrowingService.allBorrowings(status, PageRequest.of(page, size, Sort.by("createdAt").descending()));
        return ResponseEntity.ok(PageResponse.from(result.map(BorrowingResponse::fromEntity)));
    }

    private String currentUserId(Authentication auth) {
        return (String) auth.getPrincipal();
    }

    private boolean isAdmin(Authentication auth) {
        for (GrantedAuthority a : auth.getAuthorities()) {
            if (a.getAuthority().equals("ROLE_ADMIN")) return true;
        }
        return false;
    }
}
