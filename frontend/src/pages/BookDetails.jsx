import { useEffect, useState } from "react";
import { useParams, useNavigate, Link } from "react-router-dom";
import api from "../api/axios";
import { useAuth } from "../context/AuthContext";
import LoadingState from "../components/LoadingState";
import ErrorBanner from "../components/ErrorBanner";

export default function BookDetails() {
  const { id } = useParams();
  const { user } = useAuth();
  const navigate = useNavigate();
  const [book, setBook] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [borrowing, setBorrowing] = useState(false);
  const [success, setSuccess] = useState("");

  useEffect(() => {
    api.get(`/books/${id}`)
      .then(({ data }) => setBook(data))
      .catch(() => setError("This book could not be found."))
      .finally(() => setLoading(false));
  }, [id]);

  async function handleBorrow() {
    if (!user) {
      navigate("/login", { state: { from: `/books/${id}` } });
      return;
    }
    setBorrowing(true);
    setError("");
    setSuccess("");
    try {
      await api.post("/borrowings", { bookId: id });
      setSuccess("Borrowed! Check \"My Borrowings\" for your due date.");
      const { data } = await api.get(`/books/${id}`);
      setBook(data);
    } catch (err) {
      setError(err.response?.data?.message || "Could not borrow this book right now.");
    } finally {
      setBorrowing(false);
    }
  }

  if (loading) return <div className="page-container"><LoadingState label="Loading book" /></div>;
  if (!book) return <div className="page-container"><ErrorBanner message={error || "Book not found."} /></div>;

  const canBorrow = book.status === "ACTIVE" && book.availableCopies > 0;

  return (
    <div className="page-container book-details">
      <Link to="/books" className="back-link">← Back to catalogue</Link>

      <div className="book-details-layout">
        <div className="book-spine" aria-hidden="true">
          <span>{book.category}</span>
        </div>

        <div className="book-details-main">
          <p className="eyebrow">{book.category}</p>
          <h1>{book.title}</h1>
          <p className="book-author-large">by {book.author}</p>

          {book.isbn && <p className="isbn">ISBN {book.isbn}</p>}

          <p className="book-description">{book.description || "No description provided."}</p>

          <div className="availability-box">
            <div>
              <strong>{book.availableCopies}</strong> of <strong>{book.totalCopies}</strong> copies available
            </div>
            <div className={`availability-tag ${canBorrow ? "ok" : "none"}`}>
              {canBorrow ? "Available to borrow" : "Not available"}
            </div>
          </div>

          <ErrorBanner message={error} />
          {success && <div className="success-banner">{success}</div>}

          <button
            className="btn btn-primary btn-large"
            onClick={handleBorrow}
            disabled={!canBorrow || borrowing}
          >
            {borrowing ? "Borrowing…" : canBorrow ? "Borrow this book" : "Unavailable"}
          </button>
        </div>
      </div>
    </div>
  );
}
