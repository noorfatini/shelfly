import { useCallback, useEffect, useState } from "react";
import api from "../../api/axios";
import LoadingState from "../../components/LoadingState";
import EmptyState from "../../components/EmptyState";
import ErrorBanner from "../../components/ErrorBanner";
import Pagination from "../../components/Pagination";
import StatusStamp from "../../components/StatusStamp";

const emptyForm = { title: "", author: "", isbn: "", category: "", description: "", totalCopies: 1, status: "ACTIVE" };

export default function AdminBooks() {
  const [books, setBooks] = useState([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [formOpen, setFormOpen] = useState(false);
  const [editingId, setEditingId] = useState(null);
  const [form, setForm] = useState(emptyForm);
  const [saving, setSaving] = useState(false);

  const load = useCallback(async () => {
    setLoading(true);
    setError("");
    try {
      const { data } = await api.get("/books", { params: { page, size: 8, sortBy: "title", direction: "asc" } });
      setBooks(data.content);
      setTotalPages(data.totalPages);
    } catch {
      setError("Could not load books.");
    } finally {
      setLoading(false);
    }
  }, [page]);

  useEffect(() => { load(); }, [load]);

  function openCreate() {
    setForm(emptyForm);
    setEditingId(null);
    setFormOpen(true);
  }

  function openEdit(book) {
    setForm({
      title: book.title, author: book.author, isbn: book.isbn || "",
      category: book.category, description: book.description || "",
      totalCopies: book.totalCopies, status: book.status,
    });
    setEditingId(book.id);
    setFormOpen(true);
  }

  async function handleSubmit(e) {
    e.preventDefault();
    setSaving(true);
    setError("");
    try {
      const payload = { ...form, totalCopies: Number(form.totalCopies) };
      if (editingId) {
        await api.put(`/books/${editingId}`, payload);
      } else {
        await api.post("/books", payload);
      }
      setFormOpen(false);
      await load();
    } catch (err) {
      setError(err.response?.data?.message || "Could not save this book.");
    } finally {
      setSaving(false);
    }
  }

  async function handleDeactivate(id) {
    try {
      await api.patch(`/books/${id}/deactivate`);
      await load();
    } catch (err) {
      setError(err.response?.data?.message || "Could not deactivate this book.");
    }
  }

  async function handleDelete(id) {
    if (!window.confirm("Delete this book permanently? This cannot be undone.")) return;
    try {
      await api.delete(`/books/${id}`);
      await load();
    } catch (err) {
      setError(err.response?.data?.message || "Could not delete this book.");
    }
  }

  return (
    <div className="page-container">
      <div className="page-header page-header-row">
        <div>
          <p className="eyebrow">Admin</p>
          <h1>Manage Books</h1>
        </div>
        <button className="btn btn-primary" onClick={openCreate}>+ Add Book</button>
      </div>

      <ErrorBanner message={error} />

      {formOpen && (
        <div className="panel form-panel">
          <h3>{editingId ? "Edit book" : "Add a new book"}</h3>
          <form onSubmit={handleSubmit} className="form form-grid">
            <label>Title
              <input value={form.title} onChange={(e) => setForm({ ...form, title: e.target.value })} required />
            </label>
            <label>Author
              <input value={form.author} onChange={(e) => setForm({ ...form, author: e.target.value })} required />
            </label>
            <label>ISBN
              <input value={form.isbn} onChange={(e) => setForm({ ...form, isbn: e.target.value })} />
            </label>
            <label>Category
              <input value={form.category} onChange={(e) => setForm({ ...form, category: e.target.value })} required />
            </label>
            <label>Total copies
              <input type="number" min="1" value={form.totalCopies}
                onChange={(e) => setForm({ ...form, totalCopies: e.target.value })} required />
            </label>
            <label>Status
              <select value={form.status} onChange={(e) => setForm({ ...form, status: e.target.value })}>
                <option value="ACTIVE">Active</option>
                <option value="INACTIVE">Inactive</option>
              </select>
            </label>
            <label className="form-grid-full">Description
              <textarea rows="3" value={form.description} onChange={(e) => setForm({ ...form, description: e.target.value })} />
            </label>
            <div className="form-actions form-grid-full">
              <button className="btn btn-primary" type="submit" disabled={saving}>
                {saving ? "Saving…" : editingId ? "Save changes" : "Create book"}
              </button>
              <button className="btn btn-ghost" type="button" onClick={() => setFormOpen(false)}>Cancel</button>
            </div>
          </form>
        </div>
      )}

      {loading ? (
        <LoadingState label="Loading books" />
      ) : books.length === 0 ? (
        <EmptyState title="No books yet" description="Add your first book to the catalogue." />
      ) : (
        <>
          <div className="table-wrap">
            <table className="data-table">
              <thead>
                <tr>
                  <th>Title</th><th>Category</th><th>Copies</th><th>Status</th><th></th>
                </tr>
              </thead>
              <tbody>
                {books.map((b) => (
                  <tr key={b.id}>
                    <td>
                      <div className="cell-title">{b.title}</div>
                      <div className="cell-sub">{b.author}</div>
                    </td>
                    <td>{b.category}</td>
                    <td>{b.availableCopies} / {b.totalCopies}</td>
                    <td><StatusStamp status={b.status} /></td>
                    <td className="row-actions">
                      <button className="btn btn-ghost btn-small" onClick={() => openEdit(b)}>Edit</button>
                      {b.status === "ACTIVE" && (
                        <button className="btn btn-ghost btn-small" onClick={() => handleDeactivate(b.id)}>Deactivate</button>
                      )}
                      <button className="btn btn-ghost btn-small btn-danger" onClick={() => handleDelete(b.id)}>Delete</button>
                    </td>
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
