package com.shelfly.backend.seed;

import com.shelfly.backend.model.*;
import com.shelfly.backend.repository.BookRepository;
import com.shelfly.backend.repository.BorrowingRepository;
import com.shelfly.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Seeds sample data on first run so the app is demo-ready immediately after startup.
 * Mirrors the seeder pattern used elsewhere in the course (UserDataSeeder / DataSeeder style):
 * only runs when the relevant collection is empty, so it's safe to restart the app repeatedly.
 */
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final BorrowingRepository borrowingRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        seedUsers();
        seedBooks();
    }

    private void seedUsers() {
        if (userRepository.count() > 0) {
            log.info("Users already exist, skipping user seeding");
            return;
        }

        User admin = User.builder()
                .name("Shelfly Admin")
                .email("admin@shelfly.com")
                .password(passwordEncoder.encode("Admin123!"))
                .role(Role.ADMIN)
                .active(true)
                .build();

        User athirah = User.builder()
                .name("Athirah")
                .email("member@shelfly.com")
                .password(passwordEncoder.encode("Member123!"))
                .role(Role.MEMBER)
                .active(true)
                .build();

        User nurin = User.builder()
                .name("Nurin")
                .email("nurin@shelfly.com")
                .password(passwordEncoder.encode("Nurin123!"))
                .role(Role.MEMBER)
                .active(true)
                .build();

        userRepository.save(admin);
        userRepository.save(athirah);
        userRepository.save(nurin);
        log.info("Seeded admin (admin@shelfly.com / Admin123!), Athirah (member@shelfly.com / Member123!) and Nurin (nurin@shelfly.com / Nurin123!)");
    }

    private void seedBooks() {
        if (bookRepository.count() > 0) {
            log.info("Books already exist, skipping book seeding");
            return;
        }

        List<Book> books = List.of(
                Book.builder().title("A Game of Thrones").author("George R. R. Martin").isbn("9780553593716")
                        .category("Fantasy").description("The first book in A Song of Ice and Fire — noble houses vie for the Iron Throne.")
                        .totalCopies(3).availableCopies(3).status(BookStatus.ACTIVE).build(),
                Book.builder().title("Fire & Blood").author("George R. R. Martin").isbn("9781524796280")
                        .category("Fantasy").description("A history of House Targaryen, and the basis for House of the Dragon.")
                        .totalCopies(2).availableCopies(2).status(BookStatus.ACTIVE).build(),
                Book.builder().title("The Witherward").author("Hannah Mathewson").isbn("9781542040079")
                        .category("Fantasy").description("A hidden mirror-London of magical factions, and the girl caught between two worlds.")
                        .totalCopies(2).availableCopies(2).status(BookStatus.ACTIVE).build(),
                Book.builder().title("The Name of the Wind").author("Patrick Rothfuss").isbn("9780756404741")
                        .category("Fantasy").description("The storied life of Kvothe, told in his own words.")
                        .totalCopies(3).availableCopies(3).status(BookStatus.ACTIVE).build(),
                Book.builder().title("A Court of Thorns and Roses").author("Sarah J. Maas").isbn("9781635575569")
                        .category("Romance").description("A huntress is dragged into a treacherous, seductive faerie court.")
                        .totalCopies(4).availableCopies(4).status(BookStatus.ACTIVE).build(),
                Book.builder().title("Dune").author("Frank Herbert").isbn("9780441172719")
                        .category("Science Fiction").description("A science fiction epic set on the desert planet Arrakis.")
                        .totalCopies(4).availableCopies(4).status(BookStatus.ACTIVE).build(),
                Book.builder().title("Sapiens").author("Yuval Noah Harari").isbn("9780062316097")
                        .category("Non-Fiction").description("A brief history of humankind.")
                        .totalCopies(3).availableCopies(3).status(BookStatus.ACTIVE).build(),
                Book.builder().title("Atomic Habits").author("James Clear").isbn("9780735211292")
                        .category("Self-Help").description("An easy and proven way to build good habits.")
                        .totalCopies(3).availableCopies(3).status(BookStatus.ACTIVE).build(),
                Book.builder().title("The Great Gatsby").author("F. Scott Fitzgerald").isbn("9780743273565")
                        .category("Classic Fiction").description("Jazz Age excess and longing on Long Island.")
                        .totalCopies(5).availableCopies(5).status(BookStatus.ACTIVE).build(),
                Book.builder().title("The Silent Patient").author("Alex Michaelides").isbn("9781250301697")
                        .category("Mystery & Thriller").description("A psychotherapist becomes obsessed with treating a woman who won't speak.")
                        .totalCopies(2).availableCopies(2).status(BookStatus.INACTIVE).build()
        );

        bookRepository.saveAll(books);
        log.info("Seeded {} sample books", books.size());

        seedBorrowings();
    }

    /**
     * Seeds borrowings that deliberately cover every status/condition the demo needs to show:
     * a normal active borrow, a returned book, an overdue book, and a member sitting exactly
     * at the active-borrowing limit (so attempting one more triggers the business rule live).
     */
    private void seedBorrowings() {
        User athirah = userRepository.findByEmail("member@shelfly.com").orElse(null);
        User nurin = userRepository.findByEmail("nurin@shelfly.com").orElse(null);
        if (athirah == null || nurin == null) return;

        Instant now = Instant.now();

        // Athirah: one active borrow, one returned, one overdue -- shows all three states.
        borrowBook(athirah, "The Witherward", now.minus(3, ChronoUnit.DAYS), now.plus(11, ChronoUnit.DAYS),
                BorrowingStatus.BORROWED, null);
        borrowBook(athirah, "Dune", now.minus(20, ChronoUnit.DAYS), now.minus(6, ChronoUnit.DAYS),
                BorrowingStatus.RETURNED, now.minus(2, ChronoUnit.DAYS));
        borrowBook(athirah, "A Game of Thrones", now.minus(20, ChronoUnit.DAYS), now.minus(6, ChronoUnit.DAYS),
                BorrowingStatus.OVERDUE, null);

        // Nurin: exactly 3 active borrowings -- at SHELFLY_MAX_ACTIVE_BORROWINGS, so trying to
        // borrow a 4th book in the demo will correctly hit "maximum active borrowings" live.
        borrowBook(nurin, "Fire & Blood", now.minus(2, ChronoUnit.DAYS), now.plus(12, ChronoUnit.DAYS),
                BorrowingStatus.BORROWED, null);
        borrowBook(nurin, "A Court of Thorns and Roses", now.minus(5, ChronoUnit.DAYS), now.plus(9, ChronoUnit.DAYS),
                BorrowingStatus.BORROWED, null);
        borrowBook(nurin, "Sapiens", now.minus(1, ChronoUnit.DAYS), now.plus(13, ChronoUnit.DAYS),
                BorrowingStatus.BORROWED, null);

        log.info("Seeded sample borrowings: Athirah (borrowed, returned, overdue) and Nurin (3 active, at the limit)");
    }

    private void borrowBook(User user, String bookTitle, Instant borrowDate, Instant dueDate,
                             BorrowingStatus status, Instant returnDate) {
        Book book = bookRepository.findAll().stream()
                .filter(b -> b.getTitle().equals(bookTitle)).findFirst().orElse(null);
        if (book == null) return;

        // A returned book's copy is already back on the shelf; only an active/overdue
        // borrowing actually holds a copy out.
        if (status != BorrowingStatus.RETURNED) {
            book.setAvailableCopies(book.getAvailableCopies() - 1);
            bookRepository.save(book);
        }

        Borrowing borrowing = Borrowing.builder()
                .userId(user.getId())
                .bookId(book.getId())
                .bookTitle(book.getTitle())
                .bookCategory(book.getCategory())
                .userName(user.getName())
                .borrowDate(borrowDate)
                .dueDate(dueDate)
                .returnDate(returnDate)
                .status(status)
                .build();
        borrowingRepository.save(borrowing);
    }
}
