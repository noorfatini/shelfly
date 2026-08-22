import { useEffect, useState, useCallback } from "react";
import { Link } from "react-router-dom";
import api from "../api/axios";
import LoadingState from "../components/LoadingState";
import EmptyState from "../components/EmptyState";
import ErrorBanner from "../components/ErrorBanner";
import Pagination from "../components/Pagination";
import StatusStamp from "../components/StatusStamp";

const CATEGORIES = [
  "Software Engineering", "Databases", "Fiction", "Non-Fiction", "Self-Help", "Reference",
];

export default function BookList() {
  const [books, setBooks] = useState([]);
  const [keyword, setKeyword] = useState("");
  const [category, setCategory] = useState("");
  const [sortBy, setSortBy] = useState("title");
  const [direction, setDirection] = useState("asc");
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const fetchBooks = useCallback(async () => {
    setLoading(true);
    setError("");
    try {
      const { data } = await api.get("/books", {
        params: { keyword, category, status: "ACTIVE", sortBy, direction, page, size: 8 },
      });
      setBooks(data.content);
      setTotalPages(data.totalPages);
    } catch (err) {
      setError("Could not load the catalogue. Please try again.");
    } finally {
      setLoading(false);
    }
  }, [keyword, category, sortBy, direction, page]);

  useEffect(() => {
    fetchBooks();
  }, [fetchBooks]);

  function handleSearchSubmit(e) {
    e.preventDefault();
    setPage(0);
    fetchBooks();
  }

  return (
    <div className="page-container">
      <div className="page-header">
        <p className="eyebrow">The catalogue</p>
        <h1>Find your next read</h1>
      </div>

      <form className="filter-bar" onSubmit={handleSearchSubmit}>
        <input
          type="text"
          placeholder="Search by title or author…"
          value={keyword}
          onChange={(e) => setKeyword(e.target.value)}
        />
        <select value={category} onChange={(e) => { setCategory(e.target.value); setPage(0); }}>
          <option value="">All categories</option>
          {CATEGORIES.map((c) => <option key={c} value={c}>{c}</option>)}
        </select>
        <select value={sortBy} onChange={(e) => { setSortBy(e.target.value); setPage(0); }}>
          <option value="title">Sort: Title</option>
          <option value="author">Sort: Author</option>
          <option value="availableCopies">Sort: Availability</option>
        </select>
        <select value={direction} onChange={(e) => { setDirection(e.target.value); setPage(0); }}>
          <option value="asc">Ascending</option>
          <option value="desc">Descending</option>
        </select>
        <button className="btn btn-primary" type="submit">Search</button>
      </form>

      <ErrorBanner message={error} />

      {loading ? (
        <LoadingState label="Fetching the shelves" />
      ) : books.length === 0 ? (
        <EmptyState
          title="No books match your search"
          description="Try a different keyword or clear the category filter."
        />
      ) : (
        <>
          <div className="book-grid">
            {books.map((book) => (
              <Link to={`/books/${book.id}`} key={book.id} className="book-card">
                <div className="book-card-top">
                  <span className="index-label">{book.category}</span>
                  <StatusStamp status={book.availableCopies > 0 ? "ACTIVE" : "INACTIVE"} />
                </div>
                <h3>{book.title}</h3>
                <p className="book-author">{book.author}</p>
                <p className="book-availability">
                  {book.availableCopies} of {book.totalCopies} copies available
                </p>
              </Link>
            ))}
          </div>
          <Pagination page={page} totalPages={totalPages} onPageChange={setPage} />
        </>
      )}
    </div>
  );
}
