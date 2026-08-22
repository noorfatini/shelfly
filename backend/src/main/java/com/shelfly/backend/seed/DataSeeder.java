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

        User member = User.builder()
                .name("Aisyah Member")
                .email("member@shelfly.com")
                .password(passwordEncoder.encode("Member123!"))
                .role(Role.MEMBER)
                .active(true)
                .build();

        userRepository.save(admin);
        userRepository.save(member);
        log.info("Seeded default admin (admin@shelfly.com / Admin123!) and member (member@shelfly.com / Member123!) accounts");
    }

    private void seedBooks() {
        if (bookRepository.count() > 0) {
            log.info("Books already exist, skipping book seeding");
            return;
        }

        List<Book> books = List.of(
                Book.builder().title("Clean Code").author("Robert C. Martin").isbn("9780132350884")
                        .category("Software Engineering").description("A handbook of agile software craftsmanship.")
                        .totalCopies(3).availableCopies(3).status(BookStatus.ACTIVE).build(),
                Book.builder().title("Effective Java").author("Joshua Bloch").isbn("9780134685991")
                        .category("Software Engineering").description("Best practices for the Java platform.")
                        .totalCopies(2).availableCopies(2).status(BookStatus.ACTIVE).build(),
                Book.builder().title("Designing Data-Intensive Applications").author("Martin Kleppmann").isbn("9781449373320")
                        .category("Databases").description("The big ideas behind reliable, scalable systems.")
                        .totalCopies(2).availableCopies(2).status(BookStatus.ACTIVE).build(),
                Book.builder().title("The Pragmatic Programmer").author("David Thomas & Andrew Hunt").isbn("9780135957059")
                        .category("Software Engineering").description("Your journey to mastery.")
                        .totalCopies(2).availableCopies(2).status(BookStatus.ACTIVE).build(),
                Book.builder().title("Dune").author("Frank Herbert").isbn("9780441172719")
                        .category("Fiction").description("A science fiction epic set on the desert planet Arrakis.")
                        .totalCopies(4).availableCopies(4).status(BookStatus.ACTIVE).build(),
                Book.builder().title("Sapiens").author("Yuval Noah Harari").isbn("9780062316097")
                        .category("Non-Fiction").description("A brief history of humankind.")
                        .totalCopies(3).availableCopies(3).status(BookStatus.ACTIVE).build(),
                Book.builder().title("Atomic Habits").author("James Clear").isbn("9780735211292")
                        .category("Self-Help").description("An easy and proven way to build good habits.")
                        .totalCopies(3).availableCopies(3).status(BookStatus.ACTIVE).build(),
                Book.builder().title("MongoDB: The Definitive Guide").author("Shannon Bradshaw").isbn("9781491954461")
                        .category("Databases").description("Powerful and scalable data storage.")
                        .totalCopies(2).availableCopies(2).status(BookStatus.ACTIVE).build(),
                Book.builder().title("Out of Print Archive").author("Unknown").isbn("9780000000001")
                        .category("Reference").description("Retired reference volume kept for archival purposes.")
                        .totalCopies(1).availableCopies(1).status(BookStatus.INACTIVE).build()
        );

        bookRepository.saveAll(books);
        log.info("Seeded {} sample books", books.size());

        // Seed one sample borrowing so /my and reports have something to show immediately
        User member = userRepository.findByEmail("member@shelfly.com").orElse(null);
        Book cleanCode = bookRepository.findAll().stream()
                .filter(b -> b.getTitle().equals("Clean Code")).findFirst().orElse(null);

        if (member != null && cleanCode != null) {
            cleanCode.setAvailableCopies(cleanCode.getAvailableCopies() - 1);
            bookRepository.save(cleanCode);

            Instant now = Instant.now();
            Borrowing sample = Borrowing.builder()
                    .userId(member.getId())
                    .bookId(cleanCode.getId())
                    .bookTitle(cleanCode.getTitle())
                    .bookCategory(cleanCode.getCategory())
                    .userName(member.getName())
                    .borrowDate(now.minus(3, ChronoUnit.DAYS))
                    .dueDate(now.plus(11, ChronoUnit.DAYS))
                    .status(BorrowingStatus.BORROWED)
                    .build();
            borrowingRepository.save(sample);
            log.info("Seeded 1 sample borrowing for member@shelfly.com");
        }
    }
}
