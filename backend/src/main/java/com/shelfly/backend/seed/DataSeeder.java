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
                .name("Aisyah")
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

        // Seed one sample borrowing so /my and reports have something to show immediately
        User member = userRepository.findByEmail("member@shelfly.com").orElse(null);
        Book witherward = bookRepository.findAll().stream()
                .filter(b -> b.getTitle().equals("The Witherward")).findFirst().orElse(null);

        if (member != null && witherward != null) {
            witherward.setAvailableCopies(witherward.getAvailableCopies() - 1);
            bookRepository.save(witherward);

            Instant now = Instant.now();
            Borrowing sample = Borrowing.builder()
                    .userId(member.getId())
                    .bookId(witherward.getId())
                    .bookTitle(witherward.getTitle())
                    .bookCategory(witherward.getCategory())
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
