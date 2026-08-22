import { useCallback, useEffect, useState } from "react";
import api from "../api/axios";
import LoadingState from "../components/LoadingState";
import EmptyState from "../components/EmptyState";
import ErrorBanner from "../components/ErrorBanner";
import Pagination from "../components/Pagination";
import StatusStamp from "../components/StatusStamp";
import ConfirmDialog from "../components/ConfirmDialog";
import { formatDate } from "../utils/formatDate";
import { Link } from "react-router-dom";

export default function MyBorrowings() {
  const [borrowings, setBorrowings] = useState([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [actionError, setActionError] = useState("");
  const [returningId, setReturningId] = useState(null);
  const [confirmReturnId, setConfirmReturnId] = useState(null);

  const load = useCallback(async () => {
    setLoading(true);
    setError("");
    try {
      const { data } = await api.get("/borrowings/my", { params: { page, size: 8 } });
      setBorrowings(data.content);
      setTotalPages(data.totalPages);
    } catch {
      setError("Could not load your borrowings.");
    } finally {
      setLoading(false);
    }
  }, [page]);

  useEffect(() => { load(); }, [load]);

  async function handleReturn(id) {
    setReturningId(id);
    setActionError("");
    try {
      await api.patch(`/borrowings/${id}/return`);
      await load();
    } catch (err) {
      setActionError(err.response?.data?.message || "Could not return this book.");
    } finally {
      setReturningId(null);
    }
  }

  async function confirmReturn() {
    const id = confirmReturnId;
    setConfirmReturnId(null);
    await handleReturn(id);
  }

  return (
    <div className="page-container">
      <div className="page-header">
        <p className="eyebrow">Your record</p>
        <h1>My Borrowings</h1>
      </div>

      <ErrorBanner message={error || actionError} />

      {loading ? (
        <LoadingState label="Loading your borrowings" />
      ) : borrowings.length === 0 ? (
        <EmptyState
          title="No borrowings yet"
          description="Browse the catalogue and borrow a book to see it here."
          action={<Link className="btn btn-primary" to="/books">Browse catalogue</Link>}
        />
      ) : (
        <>
          <div className="table-wrap">
            <table className="data-table">
              <thead>
                <tr>
                  <th>Book</th>
                  <th>Borrowed</th>
                  <th>Due</th>
                  <th>Status</th>
                  <th></th>
                </tr>
              </thead>
              <tbody>
                {borrowings.map((b) => (
                  <tr key={b.id}>
                    <td>
                      <div className="cell-title">{b.bookTitle}</div>
                      <div className="cell-sub">{b.bookCategory}</div>
                    </td>
                    <td>{formatDate(b.borrowDate)}</td>
                    <td>{formatDate(b.dueDate)}</td>
                    <td><StatusStamp status={b.status} /></td>
                    <td>
                      {b.status !== "RETURNED" && (
                        <button
                          className="btn btn-ghost btn-small"
                          onClick={() => setConfirmReturnId(b.id)}
                          disabled={returningId === b.id}
                        >
                          {returningId === b.id ? "Returning…" : "Return"}
                        </button>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
          <Pagination page={page} totalPages={totalPages} onPageChange={setPage} />
        </>
      )}

      <ConfirmDialog
        open={!!confirmReturnId}
        title="Return book"
        message="Return this book now?"
        confirmLabel="Return"
        onConfirm={confirmReturn}
        onCancel={() => setConfirmReturnId(null)}
      />
    </div>
  );
}
