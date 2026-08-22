# Shelfly — Library Borrowing System

A full-stack library system built with **React**, **Spring Boot (Java 21)**, **MongoDB**, and **JWT authentication**.

Members can browse the book catalogue and borrow or return books. Admins (librarians) manage the
book catalogue, see every borrowing record, and view reports (most borrowed books, borrowings by
category, overdue books, and dashboard totals).

---

## What the system does

- **Two roles**: Admin (librarian) and Member.
- **Members** can register, log in, browse and search books, borrow a book, view their own
  borrowings, and return a book.
- **Admins** can create/edit/deactivate/delete books, see every borrowing record from every
  member, and view a reports dashboard.
- **Business rules** are enforced on the backend: a member can't borrow a book with no copies
  left, can't borrow the same book twice at once, and can't hold more than 3 active borrowings
  at a time. Returning a book updates its available copies.
- **Search, filter, sort, and pagination** on the book catalogue.
- **MongoDB aggregation reports**: most borrowed books, borrowings by category, dashboard
  summary, and overdue borrowings.

---

## Prerequisites (Windows)

- **Java 21 (JDK)**
- **Node.js 18+** and npm
- **MongoDB** running locally on the default port (`27017`)

You do **not** need Maven installed — this project includes a Maven wrapper (`mvnw.cmd`) that
downloads everything it needs automatically.

---

## 1. Start MongoDB

On Windows, MongoDB usually installs as a background service and is already running. If not,
open **Services** (search for it in the Start menu), find **MongoDB**, and start it.

No manual database setup is needed — the backend creates the collections and indexes by itself,
and fills in sample data the first time it runs (see [Sample Data](#sample-data--seed-instructions) below).

---

## 2. Run the backend

Open a terminal (PowerShell) in the project folder:

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

Wait until you see `Started BackendApplication` in the output. The API is now running at
**http://localhost:8082**.

By default it connects to MongoDB on `localhost:27017` with no password, using a database
called `shelfly`. If your MongoDB needs a username and password, set the `MONGODB_URI`
environment variable to your full connection string before running the command above, for
example:

```powershell
$env:MONGODB_URI = "mongodb://username:password@localhost:27017/shelfly"
.\mvnw.cmd spring-boot:run
```

---

## 3. Run the frontend

Open a **second** terminal:

```powershell
cd frontend
npm install
npm run dev
```

Wait for `Local: http://localhost:5173/` to appear, then open that address in your browser.

The frontend automatically forwards all `/api/...` requests to the backend on port 8082, so
make sure the backend (step 2) is already running first.

---

## Quick Start Summary

```powershell
# Terminal 1 — backend
cd backend
.\mvnw.cmd spring-boot:run

# Terminal 2 — frontend
cd frontend
npm install
npm run dev
```

Then open **http://localhost:5173** and log in with one of the accounts below.

---

## Sample Data / Seed Instructions

The backend automatically creates sample data the **first time it runs**, as long as the
database is empty. You don't need to do anything — it just happens.

It creates:

**3 user accounts:**

| Role | Email | Password |
|---|---|---|
| Admin | `admin@shelfly.com` | `Admin123!` |
| Member (Athirah) | `member@shelfly.com` | `Member123!` |
| Member (Nurin) | `nurin@shelfly.com` | `Nurin123!` |

**10 books** across different categories (Fantasy, Romance, Science Fiction, Non-Fiction,
Self-Help, Classic Fiction, Mystery & Thriller) — one of them ("The Silent Patient") is seeded
as `INACTIVE` so you can see that state right away.

**A set of borrowings that already cover every condition**, so you don't have to manually
create data before demoing:

- Athirah has one book currently **borrowed**, one already **returned**, and one that is
  **overdue**.
- Nurin already has **3 active borrowings** — the maximum allowed — so logging in as her and
  trying to borrow one more book will correctly fail with a "maximum active borrowings" error.
  This is the easiest way to demo the borrowing-limit business rule live.

**To reset and reseed** (for example, if you want a clean slate before your demo), clear the
three collections and restart the backend:

```powershell
mongosh "mongodb://localhost:27017/shelfly" --eval "db.users.deleteMany({}); db.books.deleteMany({}); db.borrowings.deleteMany({})"
```

(If your MongoDB needs auth, add your username/password to that connection string the same way
as in step 2.) Then restart the backend — it will detect the empty database and seed everything
again.

---

## Project Structure

```
shelfly/
├── backend/                 Spring Boot API (Java 21, MongoDB, JWT)
│   └── src/main/java/com/shelfly/backend/
│       ├── model/            User, Book, Borrowing + enums
│       ├── repository/       Spring Data Mongo repositories
│       ├── dto/               Request/response shapes
│       ├── service/           Business logic (Auth, Book, Borrowing, Report)
│       ├── controller/        REST endpoints
│       ├── security/          JWT filter, util, authenticated principal
│       ├── config/            Spring Security + CORS config
│       ├── exception/         Global exception handling
│       └── seed/              Sample data seeder
├── frontend/                 React (Vite) SPA
│   └── src/
│       ├── pages/             Route-level pages (+ pages/admin/)
│       ├── components/        Shared UI (navbar, tables, states, pagination, confirm dialog)
│       ├── context/           Auth context (JWT session)
│       └── api/               Axios client with auth interceptor
├── requests/shelfly.http     Example API calls for every endpoint (IntelliJ/VS Code REST Client)
├── screenshots/              Demo screenshots (see below)
└── README.md                  You are here
```

---

## API Endpoints

| Method | Endpoint | Access | Description |
|---|---|---|---|
| POST | `/api/auth/register` | Public | Create a member account |
| POST | `/api/auth/login` | Public | Log in, returns a JWT |
| GET | `/api/books` | Public | List books — supports `keyword`, `category`, `status`, `sortBy`, `direction`, `page`, `size` |
| GET | `/api/books/{id}` | Public | Book details |
| POST | `/api/books` | Admin | Create a book |
| PUT | `/api/books/{id}` | Admin | Update a book |
| PATCH | `/api/books/{id}/deactivate` | Admin | Deactivate a book |
| DELETE | `/api/books/{id}` | Admin | Delete a book (blocked if copies are on loan) |
| POST | `/api/borrowings` | Member | Borrow a book |
| GET | `/api/borrowings/my` | Member | View own borrowings (paginated) |
| PATCH | `/api/borrowings/{id}/return` | Owner or Admin | Return a borrowed book |
| GET | `/api/borrowings/all` | Admin | View all borrowing records — optional `status` filter |
| GET | `/api/reports/summary` | Admin | Dashboard counts |
| GET | `/api/reports/most-borrowed` | Admin | Top 5 most-borrowed books (aggregation) |
| GET | `/api/reports/by-category` | Admin | Borrowings grouped by category (aggregation) |
| GET | `/api/reports/overdue` | Admin | Currently overdue borrowings |

Full request/response examples are in `requests/shelfly.http`.

---

## Business Rules Implemented

- A book can't be borrowed if it has no copies left.
- A book can't be borrowed while it's marked `INACTIVE`.
- A member can't borrow the same book twice while they still have it out.
- A member can't hold more than 3 active borrowings at once.
- Returning a book adds one back to its available copies (never more than the total).
- Emails must be unique when registering.
- A book's total copies can't be edited below the number currently on loan.
- A book can't be deleted while any copies are on loan (it must be deactivated instead).
- A borrowing automatically switches from `BORROWED` to `OVERDUE` once its due date passes
  (checked every hour by a background job, and always correct live in the dashboard/reports).

## Database Design Notes

- `users.email` has a unique index — this enforces "no duplicate emails" at the database level,
  not just in the code.
- `books.title`, `books.category`, and `books.status` are indexed to make search/filter/sort fast.
- `books.isbn` is unique (and optional, since not every book needs one).
- `borrowings.userId`, `borrowings.bookId`, and `borrowings.status` are indexed, since every
  borrowing query (my borrowings, admin filters, reports) filters on one of these.
- A `Borrowing` record stores a copy of the book's title/category and the member's name at the
  time it was created, instead of only storing IDs. This is a deliberate choice: borrowing lists
  and reports are read far more often than a book or member's details change, so this avoids a
  lookup on every single read — a natural fit for MongoDB's document model.

## Bonus Features Implemented

- **Status badges** — every book and borrowing shows a clear status pill (Active, Inactive,
  Borrowed, Returned, Overdue) instead of plain text.
- **Better seed script** — sample data only loads once (it checks if the database is already
  empty first), and it's built to show every condition at once: an active borrow, a returned
  book, an overdue book, an inactive book, and a member already at the borrowing limit.
- **Extra reports** — the brief only asks for one MongoDB aggregation report; this project has
  four (dashboard summary, most-borrowed books, borrowings by category, and overdue list).
- **Responsive layout** — the navbar, book details page, and forms reflow properly on small
  screens, not just on desktop.
- **Multi-field filtering** — the catalogue combines keyword search, category filter, sort
  field, and sort direction together, plus a "Clear filters" button.
- **Custom confirmation dialogs** — deleting, deactivating, or returning a book shows a styled
  confirmation popup instead of the browser's plain alert box.

## Assumptions & Limitations

- The loan period (14 days) and the borrowing limit (3 books) can be changed via environment
  variables, but not yet from the admin screen itself — a reasonable next step if there's time.
- The overdue status is corrected by a background job that runs once an hour, so there can be
  up to an hour's delay before a borrowing's status label updates to `OVERDUE` on its own.
  The dashboard and reports don't have this delay — they always calculate "overdue" live.
- There's no payment, fines, or email notification system. This is intentional — the brief asks
  to keep scope focused, and those features weren't part of the required functionality.

---

## Screenshots

Screenshots go in the `screenshots/` folder in this repo, using the filenames below — the
images will then show up automatically right here in this README.

| File to save | What to capture |
|---|---|
| `screenshots/01-login.png` | The login page |
| `screenshots/02-catalogue.png` | The book catalogue, with a search or filter applied |
| `screenshots/03-book-details.png` | A single book's details page |
| `screenshots/04-my-borrowings.png` | "My Borrowings" showing a borrowed, returned, and overdue book together (log in as Athirah) |
| `screenshots/05-borrow-limit.png` | The "maximum active borrowings" error (log in as Nurin and try to borrow another book) |
| `screenshots/06-admin-manage-books.png` | The admin "Manage Books" page |
| `screenshots/07-admin-borrowings.png` | The admin "All Borrowings" page |
| `screenshots/08-admin-dashboard.png` | The admin reports dashboard |

![Login](screenshots/01-login.png)
![Catalogue](screenshots/02-catalogue.png)
![Book details](screenshots/03-book-details.png)
![My borrowings](screenshots/04-my-borrowings.png)
![Borrow limit reached](screenshots/05-borrow-limit.png)
![Admin manage books](screenshots/06-admin-manage-books.png)
![Admin all borrowings](screenshots/07-admin-borrowings.png)
![Admin dashboard](screenshots/08-admin-dashboard.png)
