import { useEffect, useState } from "react";
import api from "../../api/axios";
import LoadingState from "../../components/LoadingState";
import ErrorBanner from "../../components/ErrorBanner";
import { formatDate } from "../../utils/formatDate";

export default function AdminDashboard() {
  const [summary, setSummary] = useState(null);
  const [mostBorrowed, setMostBorrowed] = useState([]);
  const [byCategory, setByCategory] = useState([]);
  const [overdue, setOverdue] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    async function load() {
      setLoading(true);
      setError("");
      try {
        const [s, m, c, o] = await Promise.all([
          api.get("/reports/summary"),
          api.get("/reports/most-borrowed"),
          api.get("/reports/by-category"),
          api.get("/reports/overdue"),
        ]);
        setSummary(s.data);
        setMostBorrowed(m.data);
        setByCategory(c.data);
        setOverdue(o.data);
      } catch {
        setError("Could not load the dashboard reports.");
      } finally {
        setLoading(false);
      }
    }
    load();
  }, []);

  if (loading) return <div className="page-container"><LoadingState label="Compiling reports" /></div>;

  return (
    <div className="page-container">
      <div className="page-header">
        <p className="eyebrow">Admin</p>
        <h1>Dashboard</h1>
      </div>

      <ErrorBanner message={error} />

      {summary && (
        <div className="stat-grid">
          <StatCard label="Total titles" value={summary.totalBooks} />
          <StatCard label="Total copies" value={summary.totalCopies} />
          <StatCard label="Available now" value={summary.totalAvailableCopies} />
          <StatCard label="Active borrowings" value={summary.activeBorrowings} />
          <StatCard label="Overdue" value={summary.overdueBorrowings} accent="danger" />
          <StatCard label="Members" value={summary.totalMembers} />
        </div>
      )}

      <div className="report-columns">
        <div className="panel">
          <h3>Most borrowed books</h3>
          {mostBorrowed.length === 0 ? (
            <p className="muted">No borrowings recorded yet.</p>
          ) : (
            <ol className="ranked-list">
              {mostBorrowed.map((m) => (
                <li key={m.bookId}>
                  <span>{m.title}</span>
                  <span className="ranked-count">{m.borrowCount}</span>
                </li>
              ))}
            </ol>
          )}
        </div>

        <div className="panel">
          <h3>Borrowings by category</h3>
          {byCategory.length === 0 ? (
            <p className="muted">No borrowings recorded yet.</p>
          ) : (
            <ol className="ranked-list">
              {byCategory.map((c) => (
                <li key={c.category}>
                  <span>{c.category}</span>
                  <span className="ranked-count">{c.borrowCount}</span>
                </li>
              ))}
            </ol>
          )}
        </div>
      </div>

      <div className="panel">
        <h3>Overdue right now</h3>
        {overdue.length === 0 ? (
          <p className="muted">Nothing overdue. Nicely kept shelf.</p>
        ) : (
          <div className="table-wrap">
            <table className="data-table">
              <thead><tr><th>Member</th><th>Book</th><th>Was due</th></tr></thead>
              <tbody>
                {overdue.map((b) => (
                  <tr key={b.id}>
                    <td>{b.userName}</td>
                    <td>{b.bookTitle}</td>
                    <td>{formatDate(b.dueDate)}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
}

function StatCard({ label, value, accent }) {
  return (
    <div className={`stat-card ${accent ? "stat-" + accent : ""}`}>
      <div className="stat-value">{value}</div>
      <div className="stat-label">{label}</div>
    </div>
  );
}
