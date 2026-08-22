# Shelfly — Library Borrowing System

Full-stack capstone project: React (Vite) + Spring Boot (Java 21) + MongoDB + JWT authentication.

Members can browse the catalogue and borrow/return books. Admins (librarians) manage the
book catalogue, view every borrowing record, and see reporting/aggregation dashboards.

---

## Prerequisites

- **Java 21** (JDK)
- **Maven** (or an IDE with Maven bundled, e.g. IntelliJ IDEA — recommended)
- **Node.js** (18+) and npm
- **MongoDB Server** running locally on the default port (`27017`), no auth required for dev

---

## 1. Database

Start MongoDB locally. No manual collection setup is needed — the backend creates
collections and indexes automatically, and seeds sample data on first run.

```bash
# macOS (Homebrew)
brew services start mongodb-community

# Windows — MongoDB usually installs as a service and starts automatically.
# If not: run "MongoDB" from Services, or run mongod.exe manually.

# Linux
sudo systemctl start mongod
```

If you'd rather use **MongoDB Atlas** (cloud) instead of local Mongo, just set the
`MONGODB_URI` environment variable to your Atlas connection string before starting the
backend (see step 2) — everything else stays the same.

---

## 2. Backend Setup

The backend reads configuration from environment variables
(see `backend/src/main/resources/application.properties`). Sensible defaults are built in,
so **for local development you don't need to set anything** — just run it.

| Variable | Required | Default | Notes |
|---|---|---|---|
| `MONGODB_HOST` | No | `localhost` | |
| `MONGODB_PORT` | No | `27017` | |
| `MONGODB_DATABASE` | No | `shelfly` | |
| `MONGODB_URI` | No | *(built from the above)* | Set this instead if using Atlas |
| `JWT_SECRET` | No (dev) | built-in dev key | **Override this for any real deployment** |
| `JWT_EXPIRATION_MINUTES` | No | `60` | |
| `SERVER_PORT` | No | `8082` | |
| `SHELFLY_MAX_ACTIVE_BORROWINGS` | No | `3` | Business rule: max books a member can hold at once |
| `SHELFLY_LOAN_PERIOD_DAYS` | No | `14` | Business rule: loan period length |

**Run the backend** from the `backend/` folder:

```bash
cd backend
mvn spring-boot:run
```

> No Maven installed? Open the `backend/` folder in **IntelliJ IDEA** — it will detect
> `pom.xml`, download dependencies automatically, and you can just click the ▶ Run button
> on `BackendApplication.java`.

The API starts on **http://localhost:8082**.

On first run, `DataSeeder` automatically creates:
- An admin account: `admin@shelfly.com` / `Admin123!`
- A member account: `member@shelfly.com` / `Member123!`
- 9 sample books across several categories
- 1 sample active borrowing (so reports/dashboard aren't empty on first look)

It only seeds if the collections are empty, so it's safe to restart the app repeatedly.

---

## 3. Frontend Setup

**Install dependencies and run** from the `frontend/` folder:

```bash
cd frontend
npm install
npm run dev
```

The app starts on **http://localhost:5173**.

Vite's dev server proxies all `/api/**` requests to `http://localhost:8082` (see
`vite.config.js`), so no frontend `.env` file is needed — just make sure the backend is
running first.

---

## Quick Start Summary

```bash
# Terminal 1
brew services start mongodb-community   # or your OS's equivalent

# Terminal 2
cd backend && mvn spring-boot:run

# Terminal 3
cd frontend && npm install && npm run dev
```

Then open **http://localhost:5173** and log in with one of the seeded accounts above.

---

## Project Structure

```
shelfly/
├── backend/                 Spring Boot API (Java 21, MongoDB, JWT)
│   └── src/main/java/com/shelfly/backend/
│       ├── model/            User, Book, Borrowing + enums
│       ├── repository/       Spring Data Mongo repos + custom search
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
│       ├── components/        Shared UI (navbar, tables, states, pagination)
│       ├── context/           Auth context (JWT session)
│       └── api/               Axios client with auth interceptor
├── requests/shelfly.http     Example API calls for every endpoint (IntelliJ/VS Code REST Client)
├── screenshots/              Put your demo screenshots here before submission
└── README.md                  You are here
```

---

## API Endpoints

| Method | Endpoint | Access | Description |
|---|---|---|---|
| POST | `/api/auth/register` | Public | Create a member account |
| POST | `/api/auth/login` | Public | Log in, returns JWT |
| GET | `/api/books` | Public | List books — supports `keyword`, `category`, `status`, `sortBy`, `direction`, `page`, `size` |
| GET | `/api/books/{id}` | Public | Book details |
| POST | `/api/books` | Admin | Create a book |
| PUT | `/api/books/{id}` | Admin | Update a book |
| PATCH | `/api/books/{id}/deactivate` | Admin | Deactivate (soft delete) a book |
| DELETE | `/api/books/{id}` | Admin | Delete a book (blocked if copies are on loan) |
| POST | `/api/borrowings` | Member | Borrow a book |
| GET | `/api/borrowings/my` | Member | View own borrowings (paginated) |
| PATCH | `/api/borrowings/{id}/return` | Owner or Admin | Return a borrowed book |
| GET | `/api/borrowings/all` | Admin | View all borrowing records — optional `status` filter |
| GET | `/api/reports/summary` | Admin | Dashboard counts |
| GET | `/api/reports/most-borrowed` | Admin | Top 5 most-borrowed books (aggregation) |
| GET | `/api/reports/by-category` | Admin | Borrowings grouped by category (aggregation) |
| GET | `/api/reports/overdue` | Admin | Currently overdue borrowings |

Full request/response examples: see `requests/shelfly.http`.

---

## Business Rules Implemented

- A book cannot be borrowed if it has zero available copies.
- A book cannot be borrowed while `INACTIVE`.
- A member cannot borrow the same book twice while an active borrowing exists.
- A member cannot hold more than `SHELFLY_MAX_ACTIVE_BORROWINGS` (default 3) active borrowings.
- Returning a book increments the book's `availableCopies` and cannot exceed `totalCopies`.
- Emails must be unique at registration.
- Editing a book's `totalCopies` cannot drop below the number of copies currently on loan.
- A book cannot be hard-deleted while any copies are on loan (deactivate instead).
- Borrowings automatically flip from `BORROWED` to `OVERDUE` once `dueDate` passes
  (checked hourly via a scheduled job, and live in the `/reports/overdue` query).

## Database Design Notes

- `users.email` — unique index (enforces the business rule at the database layer, not just in code).
- `books.title`, `books.category`, `books.status` — indexed to support the search/filter/sort requirement efficiently.
- `books.isbn` — unique + sparse index (sparse because ISBN is optional).
- `borrowings.userId`, `borrowings.bookId`, `borrowings.status` — indexed since every borrowing
  query (my borrowings, admin filters, reports) filters on one of these.
- `Borrowing` documents **denormalise** `bookTitle`, `bookCategory`, and `userName` at creation
  time. This is a deliberate document-modelling choice: borrowing lists and reports are read far
  more often than books/users change, so storing a snapshot avoids a lookup/join on every read —
  a natural fit for MongoDB's document model versus a relational join.

## Assumptions & Limitations

- Loan period and max-active-borrowings are configurable via env vars but not currently
  editable from the admin UI — a reasonable stretch feature if time allows.
- Overdue status is corrected by a background job that runs hourly, so there can be up to a
  ~1 hour lag between a due date passing and the status label updating (the `/reports/overdue`
  endpoint always computes live, so the dashboard's "overdue" figures are always accurate
  even between job runs).
- No payment, fines, or notification system — intentionally out of scope per the brief's
  scope-control guidance.
