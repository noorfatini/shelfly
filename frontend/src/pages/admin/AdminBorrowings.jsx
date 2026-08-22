import { useCallback, useEffect, useState } from "react";
import api from "../../api/axios";
import LoadingState from "../../components/LoadingState";
import EmptyState from "../../components/EmptyState";
import ErrorBanner from "../../components/ErrorBanner";
import Pagination from "../../components/Pagination";
import StatusStamp from "../../components/StatusStamp";

export default function AdminBorrowings() {
  const [borrowings, setBorrowings] = useState([]);
  const [status, setStatus] = useState("");
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const load = useCallback(async () => {
    setLoading(true);
    setError("");
    try {
      const { data } = await api.get("/borrowings/all", { params: { status, page, size: 10 } });
      setBorrowings(data.content);
      setTotalPages(data.totalPages);
    } catch {
      setError("Could not load borrowing records.");
    } finally {
      setLoading(false);
    }
  }, [status, page]);

  useEffect(() => { load(); }, [load]);

  return (
    <div className="page-container">
      <div className="page-header">
        <p className="eyebrow">Admin</p>
        <h1>All Borrowings</h1>
      </div>

      <div className="filter-bar">
        <select value={status} onChange={(e) => { setStatus(e.target.value); setPage(0); }}>
          <option value="">All statuses</option>
          <option value="BORROWED">Borrowed</option>
          <option value="RETURNED">Returned</option>
          <option value="OVERDUE">Overdue</option>
        </select>
        <button
          className="btn btn-ghost"
          type="button"
          onClick={() => { setStatus(""); setPage(0); }}
        >
          Clear filters
        </button>
      </div>

      <ErrorBanner message={error} />

      {loading ? (
        <LoadingState label="Loading records" />
      ) : borrowings.length === 0 ? (
        <EmptyState title="No borrowing records found" description="Try a different status filter." />
      ) : (
        <>
          <div className="table-wrap">
            <table className="data-table">
              <thead>
                <tr><th>Member</th><th>Book</th><th>Borrowed</th><th>Due</th><th>Status</th></tr>
              </thead>
              <tbody>
                {borrowings.map((b) => (
                  <tr key={b.id}>
                    <td>{b.userName}</td>
                    <td>
                      <div className="cell-title">{b.bookTitle}</div>
                      <div className="cell-sub">{b.bookCategory}</div>
                    </td>
                    <td>{formatDate(b.borrowDate)}</td>
                    <td>{formatDate(b.dueDate)}</td>
                    <td><StatusStamp status={b.status} /></td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
          <Pagination page={page} totalPages={totalPages} onPageChange={setPage} />
        </>
      )}
    </div>
  );
}

function formatDate(iso) {
  if (!iso) return "—";
  return new Date(iso).toLocaleDateString(undefined, { year: "numeric", month: "short", day: "numeric" });
}
